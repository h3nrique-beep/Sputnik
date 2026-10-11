package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.ItemOrdemServico;
import br.edu.fag.sputnik.domain.Movimentacao;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.TipoItemOrdemServico;
import br.edu.fag.sputnik.domain.TipoMovimentacao;
import br.edu.fag.sputnik.repository.ItemOrdemServicoRepository;
import br.edu.fag.sputnik.repository.MovimentacaoRepository;
import br.edu.fag.sputnik.repository.PecaRepository;
import br.edu.fag.sputnik.tenant.EmpresaContext;
import br.edu.fag.sputnik.web.MovimentacaoForm;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Regras de peças, movimentações e reposição. */
@Service
public class PecasService {
    private final PecaRepository pecas;
    private final MovimentacaoRepository movimentacoes;
    private final ItemOrdemServicoRepository itensOrdem;
    private final EntityManager entityManager;

    public PecasService(
            PecaRepository pecas,
            MovimentacaoRepository movimentacoes,
            ItemOrdemServicoRepository itensOrdem,
            EntityManager entityManager) {
        this.pecas = pecas;
        this.movimentacoes = movimentacoes;
        this.itensOrdem = itensOrdem;
        this.entityManager = entityManager;
    }

    public List<Peca> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return pecas.findAll();
        }
        return pecas
                .findByNomeContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrFabricanteContainingIgnoreCase(
                        busca, busca, busca);
    }

    public Peca salvar(Peca peca) {
        String codigo = peca.getCodigo().trim().toUpperCase();
        if (pecas.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("Este código já está cadastrado (RN02).");
        }

        peca.setCodigo(codigo);
        if (!peca.temEstoqueDisponivel()) {
            peca.setEstoqueDisponivel(peca.getSaldo());
        }
        if (peca.getEstoqueDisponivel() > peca.getSaldo()) {
            throw new IllegalArgumentException(
                    "O estoque disponível não pode superar o saldo total.");
        }
        return pecas.save(peca);
    }

    @Transactional
    public void movimentar(MovimentacaoForm formulario, String operador) {
        Peca peca = buscarPeca(formulario.getPecaId());
        validarSaldoParaSaida(formulario, peca);

        int saldoAtualizado = calcularSaldoAtualizado(formulario, peca);
        int disponivelAtualizado =
                peca.getEstoqueDisponivel() + (saldoAtualizado - peca.getSaldo());
        peca.setSaldo(saldoAtualizado);
        peca.setEstoqueDisponivel(disponivelAtualizado);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(formulario.getTipo());
        movimentacao.setQuantidade(formulario.getQuantidade());
        movimentacao.setSaldoResultante(saldoAtualizado);
        movimentacao.setData(LocalDateTime.now());
        movimentacao.setOperador(operador);
        movimentacao.setObservacao(formulario.getObservacao());
        movimentacoes.save(movimentacao);
    }

    public List<Movimentacao> historico() {
        return movimentacoes.findAllByOrderByDataDesc();
    }

    public List<Movimentacao> relatorioMensal() {
        YearMonth mesAtual = YearMonth.now();
        return historico().stream()
                .filter(movimentacao -> YearMonth.from(movimentacao.getData()).equals(mesAtual))
                .toList();
    }

    public BigDecimal valorEstoque() {
        return pecas.findAll().stream()
                .map(peca -> peca.getPreco().multiply(BigDecimal.valueOf(peca.getSaldo())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Peca> criticas() {
        return pecas.findAll().stream()
                .filter(Peca::estaAbaixoDoMinimo)
                .sorted(
                        Comparator.comparingInt(
                                peca -> peca.getEstoqueDisponivel() - peca.getEstoqueMinimo()))
                .toList();
    }

    public int sugestao(Peca peca) {
        return (int)
                Math.min(
                        Integer.MAX_VALUE,
                        Math.max((peca.getEstoqueMinimo() * 2L) - peca.getEstoqueDisponivel(), 1L));
    }

    public BigDecimal custoReposicao() {
        return criticas().stream()
                .map(peca -> peca.getPreco().multiply(BigDecimal.valueOf(sugestao(peca))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long quantidadeZeradas() {
        return criticas().stream().filter(Peca::estaZerada).count();
    }

    public String gerarCsvReposicao() {
        StringBuilder conteudo =
                new StringBuilder("Código;Peça;Fornecedor;Saldo;Mínimo;Sugestão\n");
        for (Peca peca : criticas()) {
            conteudo.append(campoCsv(peca.getCodigo()))
                    .append(';')
                    .append(campoCsv(peca.getNome()))
                    .append(';')
                    .append(campoCsv(peca.getFornecedor()))
                    .append(';')
                    .append(peca.getSaldo())
                    .append(';')
                    .append(peca.getEstoqueMinimo())
                    .append(';')
                    .append(sugestao(peca))
                    .append('\n');
        }
        return conteudo.toString();
    }

    public List<Peca> pecasMaisUsadas() {
        Long empresaId = EmpresaContext.empresaId();
        var itens =
                empresaId == null
                        ? itensOrdem.findAll()
                        : itensOrdem.findByOrdemServicoEmpresaId(empresaId);
        return itens.stream()
                .filter(
                        item ->
                                item.getTipo() == TipoItemOrdemServico.PECA
                                        && item.getPeca() != null
                                        && item.isEstoqueBaixado())
                .collect(
                        Collectors.groupingBy(
                                ItemOrdemServico::getPeca,
                                Collectors.summingInt(ItemOrdemServico::getQuantidade)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Peca, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Peca buscarPeca(Long id) {
        Peca peca =
                EmpresaContext.exigirPertencimento(
                        pecas.findById(id)
                                .orElseThrow(
                                        () -> new IllegalArgumentException("Peça não encontrada.")),
                        "Peça não encontrada.");
        entityManager.refresh(peca, LockModeType.PESSIMISTIC_WRITE);
        return peca;
    }

    private void validarSaldoParaSaida(MovimentacaoForm formulario, Peca peca) {
        boolean saldoInsuficiente =
                formulario.getTipo() == TipoMovimentacao.SAIDA
                        && (formulario.getQuantidade() > peca.getSaldo()
                                || formulario.getQuantidade() > peca.getEstoqueDisponivel());
        if (saldoInsuficiente) {
            throw new IllegalArgumentException("Saída bloqueada: saldo insuficiente (RN03/RN05).");
        }
    }

    private int calcularSaldoAtualizado(MovimentacaoForm formulario, Peca peca) {
        if (formulario.getTipo() == TipoMovimentacao.ENTRADA) {
            try {
                return Math.addExact(peca.getSaldo(), formulario.getQuantidade());
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("A quantidade excede o limite de estoque.");
            }
        }
        return peca.getSaldo() - formulario.getQuantidade();
    }

    private String campoCsv(String valor) {
        if (valor == null) return "\"\"";
        String inicio = valor.stripLeading();
        if (!inicio.isEmpty() && "=+-@".indexOf(inicio.charAt(0)) >= 0
                || valor.startsWith("\t")
                || valor.startsWith("\r")
                || valor.startsWith("\n")) {
            valor = "'" + valor;
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}

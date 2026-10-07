package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.HistoricoOrdemServico;
import br.edu.fag.sputnik.domain.ItemOrdemServico;
import br.edu.fag.sputnik.domain.Movimentacao;
import br.edu.fag.sputnik.domain.OrdemServico;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.StatusOrdemServico;
import br.edu.fag.sputnik.domain.TipoItemOrdemServico;
import br.edu.fag.sputnik.domain.TipoMovimentacao;
import br.edu.fag.sputnik.domain.Veiculo;
import br.edu.fag.sputnik.repository.ClienteRepository;
import br.edu.fag.sputnik.repository.HistoricoOrdemServicoRepository;
import br.edu.fag.sputnik.repository.ItemOrdemServicoRepository;
import br.edu.fag.sputnik.repository.MovimentacaoRepository;
import br.edu.fag.sputnik.repository.OrdemServicoRepository;
import br.edu.fag.sputnik.repository.PecaRepository;
import br.edu.fag.sputnik.repository.VeiculoRepository;
import br.edu.fag.sputnik.tenant.EmpresaContext;
import br.edu.fag.sputnik.web.OrdemServicoForm;
import br.edu.fag.sputnik.web.OrdemServicoEdicaoForm;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Regras de ordens de serviço, itens e histórico. */
@Service
public class OrdensService {
    private final PecaRepository pecas;
    private final MovimentacaoRepository movimentacoes;
    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;
    private final OrdemServicoRepository ordens;
    private final ItemOrdemServicoRepository itensOrdem;
    private final HistoricoOrdemServicoRepository historicosOrdem;
    private final jakarta.persistence.EntityManager entityManager;

    public OrdensService(
            PecaRepository pecas,
            MovimentacaoRepository movimentacoes,
            ClienteRepository clientes,
            VeiculoRepository veiculos,
            OrdemServicoRepository ordens,
            ItemOrdemServicoRepository itensOrdem,
            HistoricoOrdemServicoRepository historicosOrdem, jakarta.persistence.EntityManager entityManager) {
        this.pecas = pecas;
        this.movimentacoes = movimentacoes;
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.ordens = ordens;
        this.itensOrdem = itensOrdem;
        this.historicosOrdem = historicosOrdem;
        this.entityManager = entityManager;
    }

    private static final String STATUS_ABERTA = StatusOrdemServico.ABERTA.name();

    public List<OrdemServico> ordens() {
        return ordens.findAllByOrderByAberturaDesc();
    }

    public long ordensAbertas() {
        return ordens().stream()
                .map(this::statusDaOrdem)
                .filter(status -> !status.encerraAtendimento() && !status.ehOrcamento())
                .count();
    }

    public Map<StatusOrdemServico, Long> resumoOrdensPorStatus() {
        return ordens().stream()
                .collect(Collectors.groupingBy(this::statusDaOrdem, Collectors.counting()));
    }

    public long ordensConcluidasNoMes() {
        YearMonth atual = YearMonth.now();
        return ordens().stream()
                .filter(ordem -> StatusOrdemServico.CONCLUIDA.name().equals(ordem.getStatus()))
                .filter(
                        ordem ->
                                ordem.getUltimaAtualizacao() != null
                                        && YearMonth.from(ordem.getUltimaAtualizacao())
                                                .equals(atual))
                .count();
    }

    public List<OrdemServico> ordensAguardandoPeca() {
        return ordens().stream()
                .filter(
                        ordem ->
                                StatusOrdemServico.AGUARDANDO_PECA.name().equals(ordem.getStatus()))
                .toList();
    }

    public List<OrdemServico> veiculosEmRetorno() {
        List<OrdemServico> todas = ordens();
        return todas.stream()
                .filter(ordem -> !statusDaOrdem(ordem).encerraAtendimento())
                .filter(
                        atual ->
                                todas.stream()
                                        .anyMatch(
                                                anterior ->
                                                        anterior.getVeiculo()
                                                                        .getId()
                                                                        .equals(
                                                                                atual.getVeiculo()
                                                                                        .getId())
                                                                && anterior.getAbertura()
                                                                        .isBefore(
                                                                                atual.getAbertura()
                                                                                        .minus(
                                                                                                180,
                                                                                                ChronoUnit
                                                                                                        .DAYS))))
                .limit(5)
                .toList();
    }

    @Transactional
    public OrdemServico abrirOrdem(
            Long clienteId,
            Long veiculoId,
            String descricao,
            Integer quilometragemAtual,
            String responsavel) {
        return criarAtendimento(
                clienteId, veiculoId, descricao, quilometragemAtual, responsavel, false);
    }

    private OrdemServico criarAtendimento(
            Long clienteId,
            Long veiculoId,
            String descricao,
            Integer quilometragemAtual,
            String responsavel,
            boolean orcamento) {
        Cliente cliente = buscarCliente(clienteId);
        Veiculo veiculo = buscarVeiculo(veiculoId);

        if (!veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException(
                    "O carro selecionado não pertence à pessoa cadastrada.");
        }

        OrdemServico ordem = new OrdemServico();
        ordem.setCliente(cliente);
        ordem.setVeiculo(veiculo);
        ordem.setDescricao(descricao);
        ordem.setQuilometragemAbertura(quilometragemAtual);
        ordem.setAbertura(LocalDateTime.now());
        ordem.setStatus(orcamento ? StatusOrdemServico.ORCAMENTO.name() : STATUS_ABERTA);
        ordem.setUltimaAtualizacao(LocalDateTime.now());
        veiculo.setQuilometragemAtual(quilometragemAtual);
        OrdemServico salva = ordens.save(ordem);
        registrarHistorico(
                salva,
                responsavel,
                orcamento
                        ? "Orçamento criado. Aguardando aprovação do cliente."
                        : "Ordem de serviço aberta.");
        return salva;
    }

    @Transactional
    public OrdemServico abrirOrdem(OrdemServicoForm form, String responsavel) {
        OrdemServico ordem =
                criarAtendimento(
                        form.getClienteId(),
                        form.getVeiculoId(),
                        form.getDescricao(),
                        form.getQuilometragemAtual(),
                        responsavel,
                        form.isOrcamento());
        ordem.setAgendamento(form.getAgendamento());
        ordem.setRetornoEm(form.getRetornoEm());
        ordem.setGarantiaAte(form.getGarantiaAte());
        ordem.setDocumento(form.getDocumento());
        OrdemServico salva = ordens.save(ordem);
        if (form.getAgendamento() != null) {
            registrarHistorico(
                    salva,
                    responsavel,
                    "Atendimento agendado para " + form.getAgendamento().toString().replace('T', ' ') + ".");
        }
        if (form.getRetornoEm() != null) {
            registrarHistorico(salva, responsavel, "Retorno preventivo programado para " + form.getRetornoEm() + ".");
        }
        if (form.getGarantiaAte() != null) {
            registrarHistorico(salva, responsavel, "Garantia registrada até " + form.getGarantiaAte() + ".");
        }
        return salva;
    }

    public OrdemServico buscarOrdem(Long id) {
        return EmpresaContext.exigirPertencimento(
                ordens.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Ordem de serviço não encontrada.")),
                "Ordem de serviço não encontrada.");
    }

    public List<ItemOrdemServico> itensDaOrdem(Long ordemId) {
        buscarOrdem(ordemId);
        return itensOrdem.findByOrdemServicoIdOrderByIdDesc(ordemId);
    }

    public List<HistoricoOrdemServico> historicoDaOrdem(Long ordemId) {
        buscarOrdem(ordemId);
        return historicosOrdem.findByOrdemServicoIdOrderByDataDesc(ordemId);
    }

    @Transactional
    public void atualizarOrdem(
            Long ordemId, OrdemServicoEdicaoForm formulario, String responsavel) {
        OrdemServico ordem = buscarOrdemParaAlterar(ordemId);
        StatusOrdemServico statusAnterior = statusDaOrdem(ordem);
        if (statusAnterior.ehOrcamento()) {
            throw new IllegalArgumentException(
                    "Aprove o orçamento para convertê-lo em ordem de serviço antes de editar o status.");
        }
        if (formulario.getStatus().ehOrcamento()) {
            throw new IllegalArgumentException("Uma ordem já aberta não pode voltar a ser orçamento.");
        }
        if (formulario.getDocumento() != null) ordem.setDocumento(formulario.getDocumento());
        ordem.setDescricao(formulario.getDescricao().trim());
        ordem.setQuilometragemAbertura(formulario.getQuilometragemAtual());
        ordem.getVeiculo().setQuilometragemAtual(formulario.getQuilometragemAtual());
        ordem.setStatus(formulario.getStatus().name());
        ordem.setUltimaAtualizacao(LocalDateTime.now());
        ordens.save(ordem);

        String evento =
                statusAnterior != formulario.getStatus()
                        ? "Status alterado de "
                                + statusAnterior.getDescricao()
                                + " para "
                                + formulario.getStatus().getDescricao()
                                + "."
                        : "Dados da ordem de serviço atualizados.";
        registrarHistorico(ordem, responsavel, evento);
    }

    @Transactional
    public void adicionarPecaNaOrdem(
            Long ordemId, Long pecaId, Integer quantidade, String responsavel) {
        OrdemServico ordem = buscarOrdemParaAlterar(ordemId);
        validarOrdemEditavel(ordem);
        Peca peca = buscarPeca(pecaId);
        boolean orcamento = statusDaOrdem(ordem).ehOrcamento();
        if (!orcamento && quantidade > peca.getEstoqueDisponivel()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente para adicionar esta peça à ordem.");
        }

        if (!orcamento) baixarEstoqueDaPeca(peca, quantidade, responsavel, ordem);

        ItemOrdemServico item = new ItemOrdemServico();
        item.setOrdemServico(ordem);
        item.setPeca(peca);
        item.setTipo(TipoItemOrdemServico.PECA);
        item.setDescricao(peca.getNome());
        item.setQuantidade(quantidade);
        item.setValorUnitario(peca.getPreco());
        item.setEstoqueBaixado(!orcamento);
        itensOrdem.save(item);
        atualizarOrdemDepoisDeItem(
                ordem, responsavel, "Peça adicionada: " + quantidade + "× " + peca.getNome() + ".");
    }

    @Transactional
    public void adicionarServicoNaOrdem(
            Long ordemId, String descricao, BigDecimal valor, String responsavel) {
        OrdemServico ordem = buscarOrdemParaAlterar(ordemId);
        validarOrdemEditavel(ordem);
        ItemOrdemServico item = new ItemOrdemServico();
        item.setOrdemServico(ordem);
        item.setTipo(TipoItemOrdemServico.SERVICO);
        item.setDescricao(descricao.trim());
        item.setQuantidade(1);
        item.setValorUnitario(valor);
        item.setEstoqueBaixado(false);
        itensOrdem.save(item);
        atualizarOrdemDepoisDeItem(
                ordem, responsavel, "Serviço adicionado: " + descricao.trim() + ".");
    }

    @Transactional
    public void aprovarOrcamento(Long ordemId, String responsavel) {
        OrdemServico ordem = buscarOrdemParaAlterar(ordemId);
        if (!statusDaOrdem(ordem).ehOrcamento()) {
            throw new IllegalArgumentException("Somente um orçamento pendente pode ser aprovado.");
        }

        for (ItemOrdemServico item : itensDaOrdem(ordemId)) {
            if (item.getPeca() == null || item.isEstoqueBaixado()) continue;
            Peca peca = buscarPeca(item.getPeca().getId());
            if (item.getQuantidade() > peca.getEstoqueDisponivel()) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para aprovar o orçamento: " + peca.getNome() + ".");
            }
            baixarEstoqueDaPeca(peca, item.getQuantidade(), responsavel, ordem);
            item.setEstoqueBaixado(true);
            itensOrdem.save(item);
        }

        ordem.setStatus(STATUS_ABERTA);
        ordem.setUltimaAtualizacao(LocalDateTime.now());
        ordens.save(ordem);
        registrarHistorico(ordem, responsavel, "Orçamento aprovado e convertido em ordem de serviço.");
    }

    private StatusOrdemServico statusDaOrdem(OrdemServico ordem) {
        try {
            return StatusOrdemServico.valueOf(ordem.getStatus());
        } catch (IllegalArgumentException exception) {
            return StatusOrdemServico.ABERTA;
        }
    }

    private void validarOrdemEditavel(OrdemServico ordem) {
        if (statusDaOrdem(ordem).encerraAtendimento()) {
            throw new IllegalArgumentException(
                    "Não é possível alterar uma ordem concluída ou cancelada.");
        }
    }

    private void atualizarOrdemDepoisDeItem(OrdemServico ordem, String responsavel, String evento) {
        StatusOrdemServico status = statusDaOrdem(ordem);
        if (!status.ehOrcamento() && status == StatusOrdemServico.ABERTA)
            ordem.setStatus(StatusOrdemServico.EM_ANDAMENTO.name());
        ordem.setUltimaAtualizacao(LocalDateTime.now());
        ordens.save(ordem);
        registrarHistorico(ordem, responsavel, evento);
    }

    private void registrarMovimentacaoDaOrdem(
            Peca peca, Integer quantidade, String responsavel, OrdemServico ordem) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(TipoMovimentacao.SAIDA);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setSaldoResultante(peca.getSaldo());
        movimentacao.setData(LocalDateTime.now());
        movimentacao.setOperador(responsavel);
        movimentacao.setObservacao("Baixa automática da OS #" + ordem.getId());
        movimentacoes.save(movimentacao);
    }

    private void baixarEstoqueDaPeca(
            Peca peca, Integer quantidade, String responsavel, OrdemServico ordem) {
        if (quantidade <= 0 || quantidade > peca.getSaldo() || quantidade > peca.getEstoqueDisponivel()) {
            throw new IllegalArgumentException("Estoque insuficiente para esta operação.");
        }
        peca.setSaldo(peca.getSaldo() - quantidade);
        peca.setEstoqueDisponivel(peca.getEstoqueDisponivel() - quantidade);
        registrarMovimentacaoDaOrdem(peca, quantidade, responsavel, ordem);
    }

    private void registrarHistorico(OrdemServico ordem, String responsavel, String descricao) {
        HistoricoOrdemServico evento = new HistoricoOrdemServico();
        evento.setOrdemServico(ordem);
        evento.setData(LocalDateTime.now());
        evento.setResponsavel(responsavel);
        evento.setDescricao(descricao);
        historicosOrdem.save(evento);
    }

    private Peca buscarPeca(Long id) {
        Peca peca = EmpresaContext.exigirPertencimento(
                pecas.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Peça não encontrada.")),
                "Peça não encontrada.");
        entityManager.flush();
        entityManager.refresh(peca, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return peca;
    }

    private OrdemServico buscarOrdemParaAlterar(Long id) {
        OrdemServico ordem = buscarOrdem(id);
        entityManager.flush();
        entityManager.refresh(ordem, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return ordem;
    }

    private Cliente buscarCliente(Long id) {
        return EmpresaContext.exigirPertencimento(
                clientes.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada.")),
                "Pessoa não encontrada.");
    }

    private Veiculo buscarVeiculo(Long id) {
        return EmpresaContext.exigirPertencimento(
                veiculos.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Carro não encontrado.")),
                "Carro não encontrado.");
    }
}

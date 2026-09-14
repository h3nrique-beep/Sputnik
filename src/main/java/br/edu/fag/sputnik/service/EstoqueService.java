package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.HistoricoOrdemServico;
import br.edu.fag.sputnik.domain.ItemOrdemServico;
import br.edu.fag.sputnik.domain.Movimentacao;
import br.edu.fag.sputnik.domain.OrdemServico;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.StatusOrdemServico;
import br.edu.fag.sputnik.domain.Veiculo;
import br.edu.fag.sputnik.web.MovimentacaoForm;
import br.edu.fag.sputnik.web.OrdemServicoEdicaoForm;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Fachada compatível com os controladores legados; delega às áreas de negócio. */
@Service
public class EstoqueService {
    private final PecasService pecasService;
    private final CadastrosService cadastrosService;
    private final OrdensService ordensService;

    public EstoqueService(
            PecasService pecasService,
            CadastrosService cadastrosService,
            OrdensService ordensService) {
        this.pecasService = pecasService;
        this.cadastrosService = cadastrosService;
        this.ordensService = ordensService;
    }

    public List<Peca> listar(String busca) {
        return pecasService.listar(busca);
    }

    public Peca salvar(Peca peca) {
        return pecasService.salvar(peca);
    }

    @Transactional
    public void movimentar(MovimentacaoForm formulario, String operador) {
        pecasService.movimentar(formulario, operador);
    }

    public List<Movimentacao> historico() {
        return pecasService.historico();
    }

    public List<Movimentacao> relatorioMensal() {
        return pecasService.relatorioMensal();
    }

    public BigDecimal valorEstoque() {
        return pecasService.valorEstoque();
    }

    public List<Peca> criticas() {
        return pecasService.criticas();
    }

    public int sugestao(Peca peca) {
        return pecasService.sugestao(peca);
    }

    public BigDecimal custoReposicao() {
        return pecasService.custoReposicao();
    }

    public long quantidadeZeradas() {
        return pecasService.quantidadeZeradas();
    }

    public String gerarCsvReposicao() {
        return pecasService.gerarCsvReposicao();
    }

    public List<Peca> pecasMaisUsadas() {
        return pecasService.pecasMaisUsadas();
    }

    public List<Cliente> clientes() {
        return cadastrosService.clientes();
    }

    public List<Cliente> clientes(String busca) {
        return cadastrosService.clientes(busca);
    }

    public Cliente salvarCliente(Cliente cliente) {
        return cadastrosService.salvarCliente(cliente);
    }

    public List<Veiculo> veiculos() {
        return cadastrosService.veiculos();
    }

    public List<Veiculo> veiculos(String busca) {
        return cadastrosService.veiculos(busca);
    }

    public List<Veiculo> veiculosVinculados() {
        return cadastrosService.veiculosVinculados();
    }

    public Veiculo salvarVeiculo(Veiculo veiculo, Long clienteId) {
        return cadastrosService.salvarVeiculo(veiculo, clienteId);
    }

    public List<OrdemServico> ordens() {
        return ordensService.ordens();
    }

    public long ordensAbertas() {
        return ordensService.ordensAbertas();
    }

    public Map<StatusOrdemServico, Long> resumoOrdensPorStatus() {
        return ordensService.resumoOrdensPorStatus();
    }

    public long ordensConcluidasNoMes() {
        return ordensService.ordensConcluidasNoMes();
    }

    public List<OrdemServico> ordensAguardandoPeca() {
        return ordensService.ordensAguardandoPeca();
    }

    public List<OrdemServico> veiculosEmRetorno() {
        return ordensService.veiculosEmRetorno();
    }

    @Transactional
    public OrdemServico abrirOrdem(
            Long clienteId,
            Long veiculoId,
            String descricao,
            Integer quilometragemAtual,
            String responsavel) {
        return ordensService.abrirOrdem(
                clienteId, veiculoId, descricao, quilometragemAtual, responsavel);
    }

    @Transactional
    public OrdemServico abrirOrdem(
            br.edu.fag.sputnik.web.OrdemServicoForm form, String responsavel) {
        return ordensService.abrirOrdem(form, responsavel);
    }

    public OrdemServico buscarOrdem(Long id) {
        return ordensService.buscarOrdem(id);
    }

    public List<ItemOrdemServico> itensDaOrdem(Long ordemId) {
        return ordensService.itensDaOrdem(ordemId);
    }

    public List<HistoricoOrdemServico> historicoDaOrdem(Long ordemId) {
        return ordensService.historicoDaOrdem(ordemId);
    }

    @Transactional
    public void atualizarOrdem(
            Long ordemId, OrdemServicoEdicaoForm formulario, String responsavel) {
        ordensService.atualizarOrdem(ordemId, formulario, responsavel);
    }

    @Transactional
    public void adicionarPecaNaOrdem(
            Long ordemId, Long pecaId, Integer quantidade, String responsavel) {
        ordensService.adicionarPecaNaOrdem(ordemId, pecaId, quantidade, responsavel);
    }

    @Transactional
    public void adicionarServicoNaOrdem(
            Long ordemId, String descricao, BigDecimal valor, String responsavel) {
        ordensService.adicionarServicoNaOrdem(ordemId, descricao, valor, responsavel);
    }

    @Transactional
    public void aprovarOrcamento(Long ordemId, String responsavel) {
        ordensService.aprovarOrcamento(ordemId, responsavel);
    }
}

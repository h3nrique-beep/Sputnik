package br.edu.fag.sputnik.controller.api.mapper;

import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.controller.api.dto.ClienteDto;
import br.edu.fag.sputnik.controller.api.dto.HistoricoDto;
import br.edu.fag.sputnik.controller.api.dto.ItemDto;
import br.edu.fag.sputnik.controller.api.dto.MovimentacaoDto;
import br.edu.fag.sputnik.controller.api.dto.OrdemResumoDto;
import br.edu.fag.sputnik.controller.api.dto.PecaDto;
import br.edu.fag.sputnik.controller.api.dto.UsuarioDto;
import br.edu.fag.sputnik.controller.api.dto.UsuarioSistemaDto;
import br.edu.fag.sputnik.controller.api.dto.VeiculoDto;
import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.HistoricoOrdemServico;
import br.edu.fag.sputnik.domain.ItemOrdemServico;
import br.edu.fag.sputnik.domain.Movimentacao;
import br.edu.fag.sputnik.domain.OrdemServico;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.UsuarioSistema;
import br.edu.fag.sputnik.domain.Veiculo;

/** Conversões dos modelos persistidos para os contratos da API. */
public final class ApiMapper {
    private ApiMapper() {}

    public static UsuarioDto usuarioDto(UsuarioAutenticado usuario) {
        return new UsuarioDto(
                usuario.usuarioId(),
                usuario.empresaId(),
                usuario.empresaNome(),
                usuario.nomeExibicao(),
                usuario.perfil().name(),
                usuario.permissoes().stream().map(Enum::name).toList(),
                usuario.trocaSenhaObrigatoria());
    }

    public static PecaDto pecaDto(Peca peca) {
        return new PecaDto(
                peca.getId(),
                peca.getCodigo(),
                peca.getNome(),
                peca.getFabricante(),
                peca.getCategoria(),
                peca.getPreco(),
                peca.getSaldo(),
                peca.getEstoqueDisponivel(),
                peca.getEstoqueMinimo(),
                peca.getLocalizacao());
    }

    public static ClienteDto clienteDto(Cliente cliente) {
        return new ClienteDto(
                cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getTelefone());
    }

    public static VeiculoDto veiculoDto(Veiculo veiculo) {
        return new VeiculoDto(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarcaModelo(),
                veiculo.getAno(),
                veiculo.getChassi(),
                veiculo.getQuilometragemAtual(),
                veiculo.getCliente() == null ? null : clienteDto(veiculo.getCliente()));
    }

    public static OrdemResumoDto ordemResumo(OrdemServico ordem) {
        return new OrdemResumoDto(
                ordem.getId(),
                ordem.getStatus(),
                ordem.getAbertura(),
                ordem.getAgendamento(),
                ordem.getRetornoEm(),
                ordem.getGarantiaAte(),
                ordem.getQuilometragemAbertura(),
                ordem.getDescricao(),
                clienteDto(ordem.getCliente()),
                veiculoDto(ordem.getVeiculo()),
                ordem.getDocumento());
    }

    public static ItemDto itemDto(ItemOrdemServico item) {
        return new ItemDto(
                item.getId(),
                item.getTipo().name(),
                item.getDescricao(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getTotal());
    }

    public static HistoricoDto historicoDto(HistoricoOrdemServico evento) {
        return new HistoricoDto(evento.getData(), evento.getResponsavel(), evento.getDescricao());
    }

    public static MovimentacaoDto movimentacaoDto(Movimentacao movimentacao) {
        return new MovimentacaoDto(
                movimentacao.getId(),
                pecaDto(movimentacao.getPeca()),
                movimentacao.getTipo().name(),
                movimentacao.getQuantidade(),
                movimentacao.getSaldoResultante(),
                movimentacao.getData(),
                movimentacao.getOperador(),
                movimentacao.getObservacao());
    }

    public static UsuarioSistemaDto usuarioSistemaDto(UsuarioSistema usuario) {
        return new UsuarioSistemaDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail() == null ? usuario.getUsuario() : usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getPermissoes().stream().map(Enum::name).toList(),
                usuario.isTrocaSenhaObrigatoria());
    }
}

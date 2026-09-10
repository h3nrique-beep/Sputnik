package br.edu.fag.sputnik.domain;

/** Estados permitidos para o ciclo de vida de uma ordem de serviço. */
public enum StatusOrdemServico {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em andamento"),
    AGUARDANDO_PECA("Aguardando peça"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOrdemServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean encerraAtendimento() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}

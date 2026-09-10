package br.edu.fag.sputnik.domain;

public enum PermissaoModulo {
    PECAS("Peças e movimentações"),
    ORDENS("Ordens de serviço"),
    RELATORIO_MENSAL("Relatório mensal"),
    REPOSICAO("Reposição");

    private final String descricao;

    PermissaoModulo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

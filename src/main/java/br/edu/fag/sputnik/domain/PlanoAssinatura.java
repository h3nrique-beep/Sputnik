package br.edu.fag.sputnik.domain;

public enum PlanoAssinatura {
    STARTER(3),
    PROFISSIONAL(10),
    PREMIUM(50);

    private final int limiteUsuarios;

    PlanoAssinatura(int limiteUsuarios) {
        this.limiteUsuarios = limiteUsuarios;
    }

    public int getLimiteUsuarios() {
        return limiteUsuarios;
    }
}

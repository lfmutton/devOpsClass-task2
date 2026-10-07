package com.exemplo.usuario.domain;

// Enum do dominio para os estados da matricula.
public enum StatusMatricula {
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO;

    // Transicao de estado: uma matricula so pode ser concluida uma vez.
    public StatusMatricula concluir() {
        if (this == CONCLUIDO) {
            throw new IllegalStateException("Matricula ja concluida");
        }
        return CONCLUIDO;
    }
}

package com.exemplo.usuario.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object para o titulo do curso.
@Embeddable
public class TituloCurso {

    @Column(name = "titulo", nullable = false)
    private String valor;

    protected TituloCurso() {
    }

    public TituloCurso(String valor) {
        this.valor = Texto.obrigatorio(valor, "Titulo e obrigatorio");
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TituloCurso that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

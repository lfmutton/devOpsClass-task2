package com.exemplo.usuario.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object para encapsular o nome do usuario.
@Embeddable
public class NomeUsuario {

    @Column(name = "nome", nullable = false)
    private String valor;

    protected NomeUsuario() {
    }

    public NomeUsuario(String valor) {
        this.valor = Texto.obrigatorio(valor, "Nome e obrigatorio");
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NomeUsuario that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

package com.exemplo.usuario.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object responsavel por encapsular as regras do e-mail.
// A validacao de formato fica em FormatoEmail.
@Embeddable
public class EmailUsuario {

    @Column(name = "email", nullable = false, unique = true)
    private String valor;

    protected EmailUsuario() {
    }

    public EmailUsuario(String valor) {
        String normalizado = Texto.obrigatorio(valor, "E-mail e obrigatorio").toLowerCase();
        this.valor = FormatoEmail.validar(normalizado);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EmailUsuario that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

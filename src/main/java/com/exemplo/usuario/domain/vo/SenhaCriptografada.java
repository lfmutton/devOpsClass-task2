package com.exemplo.usuario.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object para a senha ja criptografada.
// O dominio apenas garante que o valor existe.
// A criptografia em si acontece antes, na camada de servico, via PasswordEncoder.
@Embeddable
public class SenhaCriptografada {

    @Column(name = "senha", nullable = false)
    private String valor;

    protected SenhaCriptografada() {
    }

    public SenhaCriptografada(String valor) {
        this.valor = Texto.obrigatorio(valor, "Senha e obrigatoria");
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SenhaCriptografada that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

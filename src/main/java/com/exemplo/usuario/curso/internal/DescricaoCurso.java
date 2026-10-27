package com.exemplo.usuario.curso.internal;

import com.exemplo.usuario.shared.Texto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object para a descricao do curso.
// Nao tem identidade propria; representa apenas um valor.
@Embeddable
public class DescricaoCurso {

    @Column(name = "descricao", length = 1000)
    private String valor;

    protected DescricaoCurso() {
    }

    public DescricaoCurso(String valor) {
        // Aqui aceitamos null, pois a descricao foi modelada como opcional.
        this.valor = Texto.opcional(valor);
    }

    public String getValor() {
        return valor;
    }

    // Em Value Object, equals/hashCode comparam o valor interno.
    @Override
    public boolean equals(Object o) {
        return o instanceof DescricaoCurso that && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

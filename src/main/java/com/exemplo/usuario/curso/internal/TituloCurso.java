package com.exemplo.usuario.curso.internal;

import com.exemplo.usuario.shared.Texto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// Value Object para o titulo do curso.
// Nao tem identidade propria; representa apenas um valor.
@Embeddable
public class TituloCurso {

    @Column(name = "titulo", nullable = false)
    private String valor;

    protected TituloCurso() {
    }

    public TituloCurso(String valor) {
        // Titulo e obrigatorio: Texto.obrigatorio rejeita null/vazio e remove espacos nas pontas.
        this.valor = Texto.obrigatorio(valor, "Titulo e obrigatorio");
    }

    public String getValor() {
        return valor;
    }

    // Em Value Object, equals/hashCode comparam o valor interno.
    @Override
    public boolean equals(Object o) {
        return o instanceof TituloCurso that && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

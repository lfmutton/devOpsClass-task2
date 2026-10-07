package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Parte embutida de Matricula que cuida do ciclo de vida: status e nota final.
// As colunas continuam na tabela "matriculas" (status, nota_final).
@Embeddable
public class SituacaoMatricula {

    private static final double NOTA_MINIMA_PARA_CREDITOS = 7.0;

    // Toda nova matricula nasce EM_ANDAMENTO.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatricula status = StatusMatricula.EM_ANDAMENTO;

    @Column
    private Double notaFinal;

    public StatusMatricula getStatus() {
        return status;
    }

    public Double getNotaFinal() {
        return notaFinal;
    }

    // Conclui a matricula registrando a nota.
    // Lanca excecao se a matricula ja estiver concluida.
    public void concluir(double nota) {
        this.status = status.concluir();
        this.notaFinal = nota;
    }

    // A nota so e preenchida por concluir(), entao ter nota ja implica matricula concluida.
    public boolean deveLiberarCreditos() {
        return notaFinal != null && notaFinal >= NOTA_MINIMA_PARA_CREDITOS;
    }
}

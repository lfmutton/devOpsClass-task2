package com.exemplo.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Camada: DOMINIO.
// Parte embutida de Assinatura que guarda os saldos do usuario: creditos e moedas.
// As colunas continuam na tabela "assinaturas" (creditos_cursos, moedas).
@Embeddable
public class CarteiraAssinatura {

    // Todo novo usuario nasce com zero creditos/moedas.
    @Column(nullable = false)
    private Integer creditosCursos = 0;

    @Column(nullable = false)
    private Integer moedas = 0;

    public Integer getCreditosCursos() {
        return creditosCursos;
    }

    public Integer getMoedas() {
        return moedas;
    }

    // Metodo de dominio: adiciona creditos.
    public void adicionarCreditos(int quantidade) {
        this.creditosCursos += quantidade;
    }

    // Metodo de dominio: consome um credito, se houver saldo.
    public void consumirCredito() {
        if (this.creditosCursos <= 0) {
            throw new IllegalStateException("Usuario sem creditos disponiveis para cursos bonus.");
        }
        this.creditosCursos--;
    }
}

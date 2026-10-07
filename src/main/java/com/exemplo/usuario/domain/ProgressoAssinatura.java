package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Parte embutida de Assinatura que guarda o plano e as conclusoes do usuario.
// As colunas continuam na tabela "assinaturas" (plano, cursos_concluidos_com_sucesso).
@Embeddable
public class ProgressoAssinatura {

    private static final int CONCLUSOES_PARA_PREMIUM = 12;

    // @Enumerated(EnumType.STRING) grava o nome do enum no banco.
    // Todo novo usuario nasce no plano BASICO.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanoAssinatura plano = PlanoAssinatura.BASICO;

    @Column(nullable = false)
    private Integer cursosConcluidosComSucesso = 0;

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public Integer getCursosConcluidosComSucesso() {
        return cursosConcluidosComSucesso;
    }

    // Soma uma conclusao e promove para PREMIUM ao atingir 12 conclusoes.
    public void registrarConclusao() {
        this.cursosConcluidosComSucesso++;
        if (this.cursosConcluidosComSucesso >= CONCLUSOES_PARA_PREMIUM) {
            this.plano = PlanoAssinatura.PREMIUM;
        }
    }
}

package com.exemplo.usuario.usuario.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Camada: DOMINIO.
// Parte embutida de Usuario com as flags de acesso a plataforma.
// As colunas continuam na tabela "usuarios" (acesso_ao_curso, plataforma_congelada).
// A mensalidade em si agora mora no modulo mensalidade; aqui so chega a resposta "esta paga?".
@Embeddable
public class AcessoPlataforma {

    @Column(nullable = false)
    private boolean acessoAoCurso = false;

    @Column(nullable = false)
    private boolean plataformaCongelada = false;

    public boolean temAcessoAoCurso() {
        return acessoAoCurso;
    }

    public boolean isPlataformaCongelada() {
        return plataformaCongelada;
    }

    // Mensalidade nao paga (ou inexistente) bloqueia o acesso e congela a plataforma.
    // As flags sao atualizadas ANTES da excecao, para que o estado fique registrado.
    public void validar(boolean mensalidadePaga, String nomeUsuario) {
        if (!mensalidadePaga) {
            this.acessoAoCurso = false;
            this.plataformaCongelada = true;
            throw new IllegalStateException("Mensalidade pendente ou inexistente: acesso a plataforma bloqueado para o usuario " + nomeUsuario);
        }
        this.acessoAoCurso = true;
        this.plataformaCongelada = false;
    }
}

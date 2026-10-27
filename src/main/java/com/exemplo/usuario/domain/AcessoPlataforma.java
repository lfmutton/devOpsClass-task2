package com.exemplo.usuario.domain;

import jakarta.persistence.*;

import java.util.Optional;

@Embeddable
public class AcessoPlataforma {
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Mensalidade mensalidade;

    @Column(nullable = false)
    private boolean acessoAoCurso = false;

    @Column(nullable = false)
    private boolean plataformaCongelada = false;

    public Mensalidade getMensalidade() {
        return mensalidade;
    }

    public void vincularMensalidade(Mensalidade mensalidade) {
        this.mensalidade = mensalidade;
    }

    public boolean temAcessoAoCurso() {
        return acessoAoCurso;
    }

    public boolean isPlataformaCongelada() {
        return plataformaCongelada;
    }

    public void validar(String nomeUsuario) {
        boolean mensalidadePaga = Optional.ofNullable(mensalidade).map(Mensalidade::isPaga).orElse(false);
        if (!mensalidadePaga) {
            this.acessoAoCurso = false;
            this.plataformaCongelada = true;
            throw new IllegalStateException("Mensalidade pendente ou inexistente: acesso a plataforma bloqueado para o usuario " + nomeUsuario);
        }
        this.acessoAoCurso = true;
        this.plataformaCongelada = false;
    }
}

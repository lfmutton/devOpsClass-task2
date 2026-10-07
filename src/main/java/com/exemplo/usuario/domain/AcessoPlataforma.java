package com.exemplo.usuario.domain;

import jakarta.persistence.*;

import java.util.Optional;

// Camada: DOMINIO.
// Parte embutida de Usuario que controla o acesso a plataforma.
// Reune a mensalidade e as flags derivadas dela, porque uma depende da outra.
// As colunas continuam na tabela "usuarios" (acesso_ao_curso, plataforma_congelada).
@Embeddable
public class AcessoPlataforma {

    // Relacao 1:1 com Mensalidade.
    // mappedBy = a outra entidade (Mensalidade) possui a FK.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Mensalidade mensalidade;

    // Diferente do Grupo_2: aqui a ausencia de mensalidade tambem bloqueia o acesso.
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

    // So ha liberacao quando existe uma mensalidade paga.
    // Lanca excecao para impedir a operacao em andamento (ex.: uma matricula)
    // quando o acesso estiver bloqueado.
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

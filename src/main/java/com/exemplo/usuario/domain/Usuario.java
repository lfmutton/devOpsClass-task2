package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Usuario e uma entidade do negocio.
// Para manter a classe pequena, as responsabilidades foram divididas em partes embutidas:
// - PerfilUsuario: nome, e-mail e senha
// - AcessoPlataforma: mensalidade e controle de acesso
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PerfilUsuario perfil;

    // Relacao 1:1 com Assinatura.
    // mappedBy = a outra entidade (Assinatura) possui a FK.
    // cascade = ALL faz persistencia em cascata.
    // orphanRemoval = remove a assinatura se ela deixar de estar vinculada.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Assinatura assinatura;

    @Embedded
    private AcessoPlataforma acesso = new AcessoPlataforma();

    protected Usuario() {
    }

    // Construtor rico do dominio.
    // Repare que a senha recebida aqui ja deve estar criptografada pela camada de service.
    // Todo novo usuario ja nasce com a assinatura padrao (BASICO).
    public Usuario(String nome, String email, String senhaCriptografada) {
        this.perfil = new PerfilUsuario(nome, email, senhaCriptografada);
        this.assinatura = new Assinatura(this);
    }

    public Long getId() {
        return id;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Assinatura getAssinatura() {
        return assinatura;
    }

    public AcessoPlataforma getAcesso() {
        return acesso;
    }

    // Metodo de dominio: valida o acesso do usuario a plataforma.
    // Diferente do Grupo_2, aqui a ausencia de mensalidade tambem bloqueia o acesso.
    public void validarAcessoPlataforma() {
        acesso.validar(perfil.getNome());
    }
}

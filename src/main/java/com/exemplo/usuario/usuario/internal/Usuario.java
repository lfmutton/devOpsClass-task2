package com.exemplo.usuario.usuario.internal;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Usuario e uma entidade do negocio.
// Para manter a classe pequena, as responsabilidades foram divididas em partes embutidas:
// - PerfilUsuario: nome, e-mail e senha
// - AcessoPlataforma: flags de acesso a plataforma
// No monolito modular o Usuario NAO conhece mais Assinatura nem Mensalidade:
// esses modulos guardam apenas o usuario_id (Long) nas proprias tabelas.
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PerfilUsuario perfil;

    @Embedded
    private AcessoPlataforma acesso = new AcessoPlataforma();

    protected Usuario() {
    }

    // Construtor rico do dominio.
    // Repare que a senha recebida aqui ja deve estar criptografada pela camada de service.
    public Usuario(String nome, String email, String senhaCriptografada) {
        this.perfil = new PerfilUsuario(nome, email, senhaCriptografada);
    }

    public Long getId() {
        return id;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public AcessoPlataforma getAcesso() {
        return acesso;
    }

    // Metodo de dominio: valida o acesso do usuario a plataforma.
    // Quem informa se a mensalidade esta paga e o modulo mensalidade (via service).
    public void validarAcessoPlataforma(boolean mensalidadePaga) {
        acesso.validar(mensalidadePaga, perfil.getNome());
    }
}

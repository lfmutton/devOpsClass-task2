package com.exemplo.usuario.domain;

import com.exemplo.usuario.domain.vo.EmailUsuario;
import com.exemplo.usuario.domain.vo.NomeUsuario;
import com.exemplo.usuario.domain.vo.SenhaCriptografada;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

// Camada: DOMINIO.
// Parte embutida de Usuario com os dados de identificacao: nome, e-mail e senha.
// As colunas continuam na tabela "usuarios" (nome, email, senha).
@Embeddable
public class PerfilUsuario {

    // Nome, e-mail e senha foram encapsulados como Value Objects.
    @Embedded
    private NomeUsuario nome;

    @Embedded
    private EmailUsuario email;

    @Embedded
    private SenhaCriptografada senha;

    protected PerfilUsuario() {
    }

    // Repare que a senha recebida aqui ja deve estar criptografada pela camada de service.
    public PerfilUsuario(String nome, String email, String senhaCriptografada) {
        this.nome = new NomeUsuario(nome);
        this.email = new EmailUsuario(email);
        this.senha = new SenhaCriptografada(senhaCriptografada);
    }

    public String getNome() {
        return nome.getValor();
    }

    public String getEmail() {
        return email.getValor();
    }

    public String getSenha() {
        return senha.getValor();
    }
}

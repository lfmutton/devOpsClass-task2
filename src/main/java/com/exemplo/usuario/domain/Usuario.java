package com.exemplo.usuario.domain;

import com.exemplo.usuario.domain.vo.EmailUsuario;
import com.exemplo.usuario.domain.vo.NomeUsuario;
import com.exemplo.usuario.domain.vo.SenhaCriptografada;
import jakarta.persistence.*;

// Camada: DOMINIO.
// Usuario e uma entidade do negocio.
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome, e-mail e senha foram encapsulados como Value Objects.
    @Embedded
    private NomeUsuario nome;

    @Embedded
    private EmailUsuario email;

    @Embedded
    private SenhaCriptografada senha;

    // Relacao 1:1 com Assinatura.
    // mappedBy = a outra entidade (Assinatura) possui a FK.
    // cascade = ALL faz persistencia em cascata.
    // orphanRemoval = remove a assinatura se ela deixar de estar vinculada.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Assinatura assinatura;

    // Relacao 1:1 com Mensalidade, no mesmo padrao usado para Assinatura.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Mensalidade mensalidade;

    // Controle de acesso a plataforma.
    // Diferente do Grupo_2: aqui a ausencia de mensalidade tambem bloqueia o acesso.
    @Column(nullable = false)
    private boolean acessoAoCurso = false;

    @Column(nullable = false)
    private boolean plataformaCongelada = false;

    protected Usuario() {
    }

    // Construtor rico do dominio.
    // Repare que a senha recebida aqui ja deve estar criptografada pela camada de service.
    public Usuario(String nome, String email, String senhaCriptografada) {
        this.nome = new NomeUsuario(nome);
        this.email = new EmailUsuario(email);
        this.senha = new SenhaCriptografada(senhaCriptografada);
    }

    public Long getId() {
        return id;
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

    public Assinatura getAssinatura() {
        return assinatura;
    }

    public Mensalidade getMensalidade() {
        return mensalidade;
    }

    public boolean temAcessoAoCurso() {
        return acessoAoCurso;
    }

    public boolean isPlataformaCongelada() {
        return plataformaCongelada;
    }

    // Alteracoes controladas do estado da entidade.
    public void alterarNome(String nome) {
        this.nome = new NomeUsuario(nome);
    }

    public void alterarEmail(String email) {
        this.email = new EmailUsuario(email);
    }

    public void alterarSenhaCriptografada(String senhaCriptografada) {
        this.senha = new SenhaCriptografada(senhaCriptografada);
    }

    // Mantem a consistencia da associacao bidirecional Usuario <-> Assinatura.
    public void vincularAssinatura(Assinatura assinatura) {
        this.assinatura = assinatura;
        if (assinatura != null && assinatura.getUsuario() != this) {
            assinatura.setUsuario(this);
        }
    }

    // Mantem a consistencia da associacao bidirecional Usuario <-> Mensalidade.
    public void vincularMensalidade(Mensalidade mensalidade) {
        this.mensalidade = mensalidade;
        if (mensalidade != null && mensalidade.getUsuario() != this) {
            mensalidade.setUsuario(this);
        }
    }

    // Metodo de dominio: valida o acesso do usuario a plataforma.
    // Diferente do Grupo_2, aqui a ausencia de mensalidade tambem bloqueia o
    // acesso: so ha liberacao quando existe uma mensalidade paga.
    // Lanca excecao para impedir a operacao em andamento (ex.: uma matricula)
    // quando o acesso estiver bloqueado.
    public void validarAcessoPlataforma() {
        if (mensalidade == null || mensalidade.isPendente()) {
            this.acessoAoCurso = false;
            this.plataformaCongelada = true;
            throw new IllegalStateException("Mensalidade pendente ou inexistente: acesso a plataforma bloqueado para o usuario " + getNome());
        }
        this.acessoAoCurso = true;
        this.plataformaCongelada = false;
    }
}

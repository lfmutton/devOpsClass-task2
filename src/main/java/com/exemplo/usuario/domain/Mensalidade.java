package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Entidade JPA que representa a mensalidade do usuario.
// Segue o mesmo padrao de relacionamento 1:1 usado em Assinatura.
@Entity
@Table(name = "mensalidades")
public class Mensalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMensalidade status;

    // Relacionamento 1:1 com Usuario.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    protected Mensalidade() {
    }

    public Mensalidade(Usuario usuario, StatusMensalidade status) {
        this.usuario = usuario;
        this.status = status;
    }

    public StatusMensalidade getStatus() {
        return status;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // Metodo de dominio: verifica se a mensalidade esta paga.
    public boolean isPaga() {
        return status == StatusMensalidade.PAGA;
    }

    // Metodo de dominio: altera o estado de pagamento (PAGA ou PENDENTE).
    public void alterarStatus(StatusMensalidade novoStatus) {
        this.status = novoStatus;
    }
}

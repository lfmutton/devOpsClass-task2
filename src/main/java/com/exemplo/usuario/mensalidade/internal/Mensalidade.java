package com.exemplo.usuario.mensalidade.internal;

import com.exemplo.usuario.mensalidade.StatusMensalidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Camada: DOMINIO (privado do modulo mensalidade).
// Entidade JPA que representa a mensalidade do usuario.
// Em vez de @OneToOne com Usuario, guarda apenas o id: assim o modulo nao depende do modulo usuario.
// A coluna continua sendo "usuario_id", entao o esquema do banco nao muda.
@Entity
@Table(name = "mensalidades")
public class Mensalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMensalidade status;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    // Construtor exigido pelo JPA.
    protected Mensalidade() {
    }

    public Mensalidade(Long usuarioId, StatusMensalidade status) {
        this.usuarioId = usuarioId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public StatusMensalidade getStatus() {
        return status;
    }

    // Metodo de dominio: verifica se a mensalidade esta paga.
    // Usa equals em vez de == para nao criar desvio (mantem a complexidade baixa).
    public boolean isPaga() {
        return StatusMensalidade.PAGA.equals(status);
    }

    // Metodo de dominio: altera o estado de pagamento (PAGA ou PENDENTE).
    public void alterarStatus(StatusMensalidade novoStatus) {
        this.status = novoStatus;
    }
}

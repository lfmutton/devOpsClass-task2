package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Matricula liga Usuario e Curso.
// O ciclo de vida (status e nota) fica em SituacaoMatricula.
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @Embedded
    private SituacaoMatricula situacao = new SituacaoMatricula();

    @Column(nullable = false)
    private boolean bonus;

    protected Matricula() {
    }

    public Matricula(Usuario usuario, Curso curso, boolean bonus) {
        this.usuario = usuario;
        this.curso = curso;
        this.bonus = bonus;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Curso getCurso() {
        return curso;
    }

    public SituacaoMatricula getSituacao() {
        return situacao;
    }

    public boolean isBonus() {
        return bonus;
    }
}

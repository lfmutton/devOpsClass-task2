package com.exemplo.usuario.matricula.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Camada: DOMINIO.
// Matricula liga um usuario a um curso.
// No monolito modular ela NAO referencia mais as entidades Usuario e Curso
// (que pertencem a outros modulos): guarda apenas os ids. As colunas da tabela
// continuam as mesmas (usuario_id, curso_id).
// O ciclo de vida (status e nota) fica em SituacaoMatricula.
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Embedded
    private SituacaoMatricula situacao = new SituacaoMatricula();

    @Column(nullable = false)
    private boolean bonus;

    // Construtor exigido pelo JPA.
    protected Matricula() {
    }

    public Matricula(Long usuarioId, Long cursoId, boolean bonus) {
        this.usuarioId = usuarioId;
        this.cursoId = cursoId;
        this.bonus = bonus;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public SituacaoMatricula getSituacao() {
        return situacao;
    }

    public boolean isBonus() {
        return bonus;
    }
}

package com.exemplo.usuario.curso.internal;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Camada: DOMINIO (interna do modulo curso).
// Curso usa Value Objects para encapsular titulo e descricao.
// E public porque o CursoService (pacote base do modulo) precisa usa-la.
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Embedded embute o Value Object dentro da tabela da entidade.
    @Embedded
    private TituloCurso titulo;

    @Embedded
    private DescricaoCurso descricao;

    // protected e suficiente para o JPA e evita uso indevido fora do dominio.
    protected Curso() {
    }

    // Construtor rico: delega validacao dos atributos para os Value Objects.
    public Curso(String titulo, String descricao) {
        this.titulo = new TituloCurso(titulo);
        this.descricao = new DescricaoCurso(descricao);
    }

    public Long getId() {
        return id;
    }

    // Repare que a entidade expoe String para fora,
    // mas internamente guarda TituloCurso.
    public String getTitulo() {
        return titulo.getValor();
    }

    // Quando todas as colunas de um @Embedded sao null, o JPA devolve o objeto embutido como null.
    public String getDescricao() {
        return descricao != null ? descricao.getValor() : null;
    }
}

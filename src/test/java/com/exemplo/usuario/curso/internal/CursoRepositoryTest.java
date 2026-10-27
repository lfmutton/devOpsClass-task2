package com.exemplo.usuario.curso.internal;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Teste de integracao com banco H2 em memoria.
// Garante que os Value Objects (@Embeddable) continuam mapeados na tabela "cursos".
@DataJpaTest
@ActiveProfiles("h2")
class CursoRepositoryTest {

    @Autowired
    private CursoRepository repository;

    @Autowired
    private EntityManager entityManager;

    // Forca a ida ao banco e a releitura das entidades.
    private Curso salvarERecarregar(Curso curso) {
        Long id = repository.save(curso).getId();
        entityManager.flush();
        entityManager.clear();
        return repository.findById(id).orElseThrow();
    }

    @Test
    void deveSalvarERelerCursoComDescricao() {
        // DADO
        var curso = new Curso(" Java ", " Curso de Java ");

        // QUANDO
        var relido = salvarERecarregar(curso);

        // ENTAO
        assertEquals("Java", relido.getTitulo());
        assertEquals("Curso de Java", relido.getDescricao());
    }

    @Test
    void deveSalvarERelerCursoSemDescricao() {
        // DADO
        var curso = new Curso("Spring", null);

        // QUANDO
        var relido = salvarERecarregar(curso);

        // ENTAO
        assertEquals("Spring", relido.getTitulo());
        assertNull(relido.getDescricao());
    }
}

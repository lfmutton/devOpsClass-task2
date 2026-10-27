package com.exemplo.usuario.matricula.internal;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao com banco H2 em memoria.
// Garante que Matricula (com SituacaoMatricula embutida) continua mapeada na tabela "matriculas".
// Como a Matricula guarda apenas usuario_id e curso_id (sem relacionamento JPA),
// ids arbitrarios funcionam e o teste nao depende das entidades de outros modulos.
@DataJpaTest
@ActiveProfiles("h2")
class MatriculaRepositoryTest {

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveSalvarERelerMatriculaConcluida() {
        // DADO
        var matricula = new Matricula(100L, 200L, true);
        matricula.getSituacao().concluir(8.5);

        // QUANDO
        Long id = matriculaRepository.save(matricula).getId();
        entityManager.flush();
        entityManager.clear();
        var relida = matriculaRepository.findById(id).orElseThrow();

        // ENTAO
        assertEquals(100L, relida.getUsuarioId());
        assertEquals(200L, relida.getCursoId());
        assertTrue(relida.isBonus());
        assertEquals(StatusMatricula.CONCLUIDO, relida.getSituacao().getStatus());
        assertEquals(8.5, relida.getSituacao().getNotaFinal());
    }

    @Test
    void deveBuscarMatriculasPorUsuarioId() {
        // DADO
        matriculaRepository.save(new Matricula(100L, 200L, false));
        matriculaRepository.save(new Matricula(100L, 201L, true));
        matriculaRepository.save(new Matricula(101L, 200L, false));
        entityManager.flush();
        entityManager.clear();

        // QUANDO
        var doUsuario = matriculaRepository.findByUsuarioId(100L);

        // ENTAO
        assertEquals(2, doUsuario.size());
        assertTrue(doUsuario.stream().allMatch(m -> m.getUsuarioId().equals(100L)));
        assertEquals(StatusMatricula.EM_ANDAMENTO, doUsuario.get(0).getSituacao().getStatus());
    }
}

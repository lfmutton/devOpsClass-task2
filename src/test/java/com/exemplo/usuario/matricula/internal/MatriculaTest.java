package com.exemplo.usuario.matricula.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testes de dominio da Matricula (sem Spring e sem banco).
class MatriculaTest {

    private Matricula novaMatricula() {
        return new Matricula(1L, 2L, false);
    }

    @Test
    void quandoIniciadaUmaMatricula() {
        // DADO
        Long usuarioId = 1L;
        Long cursoId = 2L;

        // QUANDO
        var matricula = new Matricula(usuarioId, cursoId, true);

        // ENTAO
        assertEquals(usuarioId, matricula.getUsuarioId());
        assertEquals(cursoId, matricula.getCursoId());
        assertNull(matricula.getId());
        assertTrue(matricula.isBonus());
        assertEquals(StatusMatricula.EM_ANDAMENTO, matricula.getSituacao().getStatus());
        assertNull(matricula.getSituacao().getNotaFinal());
        assertFalse(matricula.getSituacao().deveLiberarCreditos());
    }

    @Test
    void deveLiberarCreditosQuandoConcluidaComNotaMaiorOuIgualASete() {
        // DADO
        var matricula = novaMatricula();

        // QUANDO
        matricula.getSituacao().concluir(7.0);

        // ENTAO
        assertEquals(StatusMatricula.CONCLUIDO, matricula.getSituacao().getStatus());
        assertEquals(7.0, matricula.getSituacao().getNotaFinal());
        assertTrue(matricula.getSituacao().deveLiberarCreditos());
    }

    @Test
    void naoDeveLiberarCreditosQuandoConcluidaComNotaAbaixoDeSete() {
        // DADO
        var matricula = novaMatricula();

        // QUANDO
        matricula.getSituacao().concluir(6.99);

        // ENTAO
        assertEquals(StatusMatricula.CONCLUIDO, matricula.getSituacao().getStatus());
        assertFalse(matricula.getSituacao().deveLiberarCreditos());
    }

    @Test
    void naoDevePermitirConcluirAMesmaMatriculaDuasVezes() {
        // DADO
        var matricula = novaMatricula();
        matricula.getSituacao().concluir(8.0);

        // QUANDO / ENTAO
        var ex = assertThrows(IllegalStateException.class, () -> matricula.getSituacao().concluir(10.0));
        assertEquals("Matricula ja concluida", ex.getMessage());
        // A nota original e preservada.
        assertEquals(8.0, matricula.getSituacao().getNotaFinal());
    }

    @Test
    void statusCanceladoPodeSerConcluido() {
        // Regra atual: so CONCLUIDO bloqueia uma nova conclusao.
        assertEquals(StatusMatricula.CONCLUIDO, StatusMatricula.CANCELADO.concluir());
        assertEquals(StatusMatricula.CONCLUIDO, StatusMatricula.EM_ANDAMENTO.concluir());
    }
}

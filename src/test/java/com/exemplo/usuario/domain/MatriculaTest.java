package com.exemplo.usuario.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatriculaTest {

    @Test
    void quandoIniciadaUmaMatricula() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var curso = new Curso("Java", "Curso de Java");

        // QUANDO
        var matricula = new Matricula(usuario, curso, false);

        // ENTAO
        assertEquals(StatusMatricula.EM_ANDAMENTO, matricula.getStatus());
        assertFalse(matricula.deveLiberarCreditos());
    }

    @Test
    void naoDeveLiberarCreditosAntesDeSerConcluida() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);

        // QUANDO
        matricula.setNotaFinal(9.0);

        // ENTAO
        // Mesmo com nota alta, sem status CONCLUIDO nao ha liberacao de creditos.
        assertFalse(matricula.deveLiberarCreditos());
    }

    @Test
    void deveLiberarCreditosQuandoConcluidaComNotaMaiorOuIgualASete() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);

        // QUANDO
        matricula.setStatus(StatusMatricula.CONCLUIDO);
        matricula.setNotaFinal(7.0);

        // ENTAO
        assertEquals(StatusMatricula.CONCLUIDO, matricula.getStatus());
        assertTrue(matricula.deveLiberarCreditos());
    }

    @Test
    void naoDeveLiberarCreditosQuandoConcluidaComNotaAbaixoDeSete() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);

        // QUANDO
        matricula.setStatus(StatusMatricula.CONCLUIDO);
        matricula.setNotaFinal(5.0);

        // ENTAO
        assertFalse(matricula.deveLiberarCreditos());
    }
}

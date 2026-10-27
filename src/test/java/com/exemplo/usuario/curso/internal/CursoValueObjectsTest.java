package com.exemplo.usuario.curso.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Testes dos Value Objects do modulo curso: TituloCurso e DescricaoCurso.
class CursoValueObjectsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void tituloDeveSerObrigatorio(String valor) {
        // QUANDO
        var erro = assertThrows(IllegalArgumentException.class, () -> new TituloCurso(valor));

        // ENTAO
        assertEquals("Titulo e obrigatorio", erro.getMessage());
    }

    @Test
    void tituloDeveRemoverEspacosNasPontas() {
        // DADO / QUANDO
        var titulo = new TituloCurso("  Java ");

        // ENTAO
        assertEquals("Java", titulo.getValor());
    }

    @Test
    void descricaoDeveRemoverEspacosNasPontas() {
        // DADO / QUANDO
        var descricao = new DescricaoCurso("  Curso de Java ");

        // ENTAO
        assertEquals("Curso de Java", descricao.getValor());
    }

    @Test
    void descricaoDeveAceitarNull() {
        // DADO / QUANDO
        var descricao = new DescricaoCurso(null);

        // ENTAO
        assertNull(descricao.getValor());
    }

    @Test
    void tituloDeveCompararPeloValor() {
        // DADO
        var a = new TituloCurso("Java");
        var b = new TituloCurso(" Java ");

        // QUANDO / ENTAO
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new TituloCurso("Spring"));
        assertNotEquals(a, "Java");
    }

    @Test
    void descricaoDeveCompararPeloValor() {
        // DADO
        var a = new DescricaoCurso("d");
        var b = new DescricaoCurso("d");

        // QUANDO / ENTAO
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(new DescricaoCurso(null), new DescricaoCurso(null));
        assertNotEquals(a, new DescricaoCurso("outra"));
        assertNotEquals(a, "d");
    }
}

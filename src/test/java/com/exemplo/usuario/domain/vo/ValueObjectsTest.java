package com.exemplo.usuario.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueObjectsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void textosObrigatoriosRejeitamValorVazio(String valor) {
        assertEquals("Nome e obrigatorio",
                assertThrows(IllegalArgumentException.class, () -> new NomeUsuario(valor)).getMessage());
        assertEquals("Senha e obrigatoria",
                assertThrows(IllegalArgumentException.class, () -> new SenhaCriptografada(valor)).getMessage());
        assertEquals("Titulo e obrigatorio",
                assertThrows(IllegalArgumentException.class, () -> new TituloCurso(valor)).getMessage());
        assertEquals("E-mail e obrigatorio",
                assertThrows(IllegalArgumentException.class, () -> new EmailUsuario(valor)).getMessage());
    }

    @Test
    void textosSaoGuardadosSemEspacosNasPontas() {
        assertEquals("Fulano", new NomeUsuario("  Fulano  ").getValor());
        assertEquals("hash", new SenhaCriptografada(" hash ").getValor());
        assertEquals("Java", new TituloCurso(" Java ").getValor());
        assertEquals("Curso de Java", new DescricaoCurso("  Curso de Java ").getValor());
    }

    @Test
    void descricaoAceitaNulo() {
        assertNull(new DescricaoCurso(null).getValor());
    }

    @Test
    void emailENormalizadoParaMinusculas() {
        assertEquals("fulano@teste.com", new EmailUsuario("  Fulano@TESTE.com ").getValor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "fulano@", "fulano@teste", "fulano teste@x.com"})
    void emailComFormatoInvalidoERejeitado(String email) {
        var ex = assertThrows(IllegalArgumentException.class, () -> new EmailUsuario(email));
        assertEquals("E-mail invalido", ex.getMessage());
    }

    @Test
    void valueObjectsSaoComparadosPeloValor() {
        assertEquals(new NomeUsuario("Fulano"), new NomeUsuario(" Fulano "));
        assertEquals(new NomeUsuario("Fulano").hashCode(), new NomeUsuario("Fulano").hashCode());
        assertNotEquals(new NomeUsuario("Fulano"), new NomeUsuario("Ciclano"));
        assertNotEquals(new NomeUsuario("Fulano"), "Fulano");

        assertEquals(new EmailUsuario("a@b.com"), new EmailUsuario("A@B.COM"));
        assertEquals(new EmailUsuario("a@b.com").hashCode(), new EmailUsuario("A@B.COM").hashCode());
        assertNotEquals(new EmailUsuario("a@b.com"), "a@b.com");

        assertEquals(new SenhaCriptografada("x"), new SenhaCriptografada("x"));
        assertEquals(new SenhaCriptografada("x").hashCode(), new SenhaCriptografada("x").hashCode());
        assertNotEquals(new SenhaCriptografada("x"), "x");

        assertEquals(new TituloCurso("Java"), new TituloCurso("Java"));
        assertEquals(new TituloCurso("Java").hashCode(), new TituloCurso("Java").hashCode());
        assertNotEquals(new TituloCurso("Java"), "Java");

        assertEquals(new DescricaoCurso(null), new DescricaoCurso(null));
        assertEquals(new DescricaoCurso("d").hashCode(), new DescricaoCurso("d").hashCode());
        assertNotEquals(new DescricaoCurso("d"), "d");
    }
}

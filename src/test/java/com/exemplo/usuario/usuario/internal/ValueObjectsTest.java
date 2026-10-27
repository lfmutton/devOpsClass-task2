package com.exemplo.usuario.usuario.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Value Objects do modulo usuario: NomeUsuario, EmailUsuario e SenhaCriptografada.
class ValueObjectsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void textosObrigatoriosRejeitamValorVazio(String valor) {
        // QUANDO / ENTAO
        assertEquals("Nome e obrigatorio",
                assertThrows(IllegalArgumentException.class, () -> new NomeUsuario(valor)).getMessage());
        assertEquals("Senha e obrigatoria",
                assertThrows(IllegalArgumentException.class, () -> new SenhaCriptografada(valor)).getMessage());
        assertEquals("E-mail e obrigatorio",
                assertThrows(IllegalArgumentException.class, () -> new EmailUsuario(valor)).getMessage());
    }

    @Test
    void textosSaoGuardadosSemEspacosNasPontas() {
        // QUANDO / ENTAO
        assertEquals("Fulano", new NomeUsuario("  Fulano  ").getValor());
        assertEquals("hash", new SenhaCriptografada(" hash ").getValor());
    }

    @Test
    void emailENormalizadoParaMinusculas() {
        // QUANDO / ENTAO
        assertEquals("fulano@teste.com", new EmailUsuario("  Fulano@TESTE.com ").getValor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "fulano@", "fulano@teste", "fulano teste@x.com"})
    void emailComFormatoInvalidoERejeitado(String email) {
        // QUANDO
        var ex = assertThrows(IllegalArgumentException.class, () -> new EmailUsuario(email));

        // ENTAO
        assertEquals("E-mail invalido", ex.getMessage());
    }

    @Test
    void valueObjectsSaoComparadosPeloValor() {
        // QUANDO / ENTAO
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
    }
}

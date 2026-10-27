package com.exemplo.usuario.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Testes unitarios do modulo SHARED (Texto e RecursoNaoEncontradoException).
class TextoTest {

    @Test
    void obrigatorioDeveRemoverEspacosDasPontas() {
        // DADO um texto com espacos nas pontas
        // QUANDO validamos como obrigatorio
        String resultado = Texto.obrigatorio("  Ana  ", "erro");
        // ENTAO o texto volta sem os espacos
        assertEquals("Ana", resultado);
    }

    @Test
    void obrigatorioDeveRejeitarNuloEVazio() {
        // DADO valores nulo e so com espacos
        // QUANDO validamos como obrigatorio
        // ENTAO a mensagem de erro informada e usada
        IllegalArgumentException nulo = assertThrows(IllegalArgumentException.class,
                () -> Texto.obrigatorio(null, "Nome obrigatorio"));
        IllegalArgumentException vazio = assertThrows(IllegalArgumentException.class,
                () -> Texto.obrigatorio("   ", "Nome obrigatorio"));
        assertEquals("Nome obrigatorio", nulo.getMessage());
        assertEquals("Nome obrigatorio", vazio.getMessage());
    }

    @Test
    void opcionalDeveAceitarNuloERemoverEspacos() {
        // DADO um valor nulo e um valor com espacos
        // QUANDO tratamos como opcional
        // ENTAO nulo continua nulo e o texto e aparado
        assertNull(Texto.opcional(null));
        assertEquals("abc", Texto.opcional(" abc "));
    }

    @Test
    void excecaoDeveCarregarAMensagemPeloSupplier() {
        // DADO um supplier criado com a mensagem
        // QUANDO pedimos a excecao
        RecursoNaoEncontradoException ex = RecursoNaoEncontradoException.com("Curso nao encontrado").get();
        // ENTAO a mensagem e preservada
        assertEquals("Curso nao encontrado", ex.getMessage());
    }
}

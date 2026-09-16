package com.exemplo.usuario.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioMensalidadeTest {

    @Test
    void deveBloquearAcessoQuandoNaoHaMensalidade() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, usuario::validarAcessoPlataforma);
        assertFalse(usuario.temAcessoAoCurso());
        assertTrue(usuario.isPlataformaCongelada());
    }

    @Test
    void deveBloquearAcessoQuandoMensalidadeNaoForPaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var mensalidade = new Mensalidade(usuario, StatusMensalidade.PENDENTE);
        usuario.vincularMensalidade(mensalidade);

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, usuario::validarAcessoPlataforma);
        assertFalse(usuario.temAcessoAoCurso());
        assertTrue(usuario.isPlataformaCongelada());
    }

    @Test
    void deveLiberarAcessoQuandoMensalidadeEstiverPaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var mensalidade = new Mensalidade(usuario, StatusMensalidade.PAGA);
        usuario.vincularMensalidade(mensalidade);

        // QUANDO
        usuario.validarAcessoPlataforma();

        // ENTAO
        assertTrue(usuario.temAcessoAoCurso());
        assertFalse(usuario.isPlataformaCongelada());
    }
}

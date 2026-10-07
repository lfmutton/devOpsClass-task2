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
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveBloquearAcessoQuandoMensalidadeNaoForPaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PENDENTE));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, usuario::validarAcessoPlataforma);
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveLiberarAcessoQuandoMensalidadeEstiverPaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));

        // QUANDO
        usuario.validarAcessoPlataforma();

        // ENTAO
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveVoltarABloquearQuandoMensalidadePagaFicarPendente() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var mensalidade = new Mensalidade(usuario, StatusMensalidade.PAGA);
        usuario.getAcesso().vincularMensalidade(mensalidade);
        usuario.validarAcessoPlataforma();

        // QUANDO
        mensalidade.alterarStatus(StatusMensalidade.PENDENTE);

        // ENTAO
        assertFalse(mensalidade.isPaga());
        assertThrows(IllegalStateException.class, usuario::validarAcessoPlataforma);
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
    }
}

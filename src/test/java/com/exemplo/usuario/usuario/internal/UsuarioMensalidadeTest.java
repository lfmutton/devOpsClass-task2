package com.exemplo.usuario.usuario.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regras de AcessoPlataforma: o usuario so recebe a resposta "mensalidade paga?"
// (quem guarda a mensalidade agora e o modulo mensalidade).
class UsuarioMensalidadeTest {

    private Usuario novoUsuario() {
        return new Usuario("Fulano", "fulano@teste.com", "senha123");
    }

    @Test
    void usuarioNovoNasceSemAcessoESemCongelamento() {
        // QUANDO
        var usuario = novoUsuario();

        // ENTAO
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveBloquearAcessoQuandoMensalidadeNaoForPaga() {
        // DADO
        var usuario = novoUsuario();

        // QUANDO
        var ex = assertThrows(IllegalStateException.class, () -> usuario.validarAcessoPlataforma(false));

        // ENTAO
        assertEquals("Mensalidade pendente ou inexistente: acesso a plataforma bloqueado para o usuario Fulano",
                ex.getMessage());
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveLiberarAcessoQuandoMensalidadeEstiverPaga() {
        // DADO
        var usuario = novoUsuario();

        // QUANDO
        usuario.validarAcessoPlataforma(true);

        // ENTAO
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveVoltarABloquearQuandoMensalidadePagaFicarPendente() {
        // DADO
        var usuario = novoUsuario();
        usuario.validarAcessoPlataforma(true);

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> usuario.validarAcessoPlataforma(false));
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveDescongelarQuandoMensalidadeVoltarASerPaga() {
        // DADO
        var usuario = novoUsuario();
        assertThrows(IllegalStateException.class, () -> usuario.validarAcessoPlataforma(false));

        // QUANDO
        usuario.validarAcessoPlataforma(true);

        // ENTAO
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
    }
}

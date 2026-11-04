package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.usuario.UsuarioCriadoEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MensalidadeInicialListenerTest {

    @Mock
    private MensalidadeService mensalidadeService;

    @Test
    void deveCriarMensalidadePagaQuandoUsuarioECriado() {
        // DADO
        var listener = new MensalidadeInicialListener(mensalidadeService);

        // QUANDO
        listener.aoCriarUsuario(new UsuarioCriadoEvent(7L));

        // ENTAO
        verify(mensalidadeService).definirStatus(7L, StatusMensalidade.PAGA);
    }
}

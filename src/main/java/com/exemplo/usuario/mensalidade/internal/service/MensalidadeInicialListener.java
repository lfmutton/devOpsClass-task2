package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.usuario.UsuarioCriadoEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Reage ao cadastro de um usuario criando a mensalidade inicial ja PAGA.
// @EventListener roda de forma sincrona, dentro da mesma transacao do cadastro:
// se algo falhar aqui, o cadastro inteiro e desfeito.
@Component
public class MensalidadeInicialListener {

    private final MensalidadeService mensalidadeService;

    public MensalidadeInicialListener(MensalidadeService mensalidadeService) {
        this.mensalidadeService = mensalidadeService;
    }

    @EventListener
    public void aoCriarUsuario(UsuarioCriadoEvent evento) {
        mensalidadeService.definirStatus(evento.usuarioId(), StatusMensalidade.PAGA);
    }
}

package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.usuario.MensalidadeConsulta;

import org.springframework.stereotype.Component;

import java.util.Optional;

// Implementa a porta MensalidadeConsulta que o modulo usuario declara.
// O Spring injeta este bean onde o usuario pede um MensalidadeConsulta,
// entao a dependencia aponta mensalidade -> usuario (e nao o contrario).
@Component
public class MensalidadeConsultaAdapter implements MensalidadeConsulta {

    private final MensalidadeService mensalidadeService;

    public MensalidadeConsultaAdapter(MensalidadeService mensalidadeService) {
        this.mensalidadeService = mensalidadeService;
    }

    @Override
    public boolean estaPaga(Long usuarioId) {
        return mensalidadeService.estaPaga(usuarioId);
    }

    @Override
    public Optional<String> statusAtual(Long usuarioId) {
        return mensalidadeService.buscarStatus(usuarioId).map(Enum::name);
    }
}

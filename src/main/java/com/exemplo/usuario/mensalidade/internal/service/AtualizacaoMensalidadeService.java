package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.mensalidade.internal.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.usuario.UsuarioApi;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE.
// Caso de uso de atualizacao da mensalidade de um usuario.
// O modulo mensalidade grava o novo status; o modulo usuario (via UsuarioApi)
// confirma que o usuario existe, reavalia as flags de acesso e devolve o usuario.
@Service
public class AtualizacaoMensalidadeService {

    private final MensalidadeService mensalidadeService;
    private final UsuarioApi usuarioApi;

    public AtualizacaoMensalidadeService(MensalidadeService mensalidadeService, UsuarioApi usuarioApi) {
        this.mensalidadeService = mensalidadeService;
        this.usuarioApi = usuarioApi;
    }

    @Transactional
    public UsuarioResponseDTO atualizarStatus(Long usuarioId, MensalidadeRequestDTO dto) {
        // Usuario inexistente -> "Usuario nao encontrado", antes de mexer na mensalidade.
        usuarioApi.verificarExistencia(usuarioId);

        // trim + maiusculas; valor invalido -> IllegalArgumentException (400).
        StatusMensalidade status = StatusMensalidade.de(dto.getStatus());
        mensalidadeService.definirStatus(usuarioId, status);

        // Bloquear ou liberar o acesso e responsabilidade do modulo usuario.
        return usuarioApi.reavaliarAcesso(usuarioId);
    }
}

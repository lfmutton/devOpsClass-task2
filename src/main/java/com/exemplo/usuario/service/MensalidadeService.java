package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE.
// Orquestra o caso de uso de atualizacao da mensalidade de um usuario.
@Service
public class MensalidadeService {

    private final UsuarioRepository usuarioRepository;

    public MensalidadeService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO atualizarStatus(Long usuarioId, MensalidadeRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(RecursoNaoEncontradoException.com("Usuario nao encontrado"));

        StatusMensalidade status = StatusMensalidade.valueOf(dto.getStatus().trim().toUpperCase());

        Mensalidade mensalidade = usuario.getAcesso().getMensalidade();
        if (mensalidade == null) {
            usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, status));
        } else {
            mensalidade.alterarStatus(status);
        }

        // Reavalia o acesso a plataforma imediatamente apos a mudanca de status.
        // Se ficar bloqueado, a excecao e apenas o sinal de que o estado foi
        // atualizado; o usuario continua sendo persistido com as flags corretas.
        try {
            usuario.validarAcessoPlataforma();
        } catch (IllegalStateException ignored) {
            // Estado ja registrado em usuario (acessoAoCurso=false, plataformaCongelada=true).
        }

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.de(salvo);
    }
}

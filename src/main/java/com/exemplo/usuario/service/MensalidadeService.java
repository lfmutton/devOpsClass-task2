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
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        StatusMensalidade status = StatusMensalidade.valueOf(dto.getStatus().trim().toUpperCase());

        Mensalidade mensalidade = usuario.getMensalidade();
        if (mensalidade == null) {
            mensalidade = new Mensalidade(usuario, status);
            usuario.vincularMensalidade(mensalidade);
        } else if (status == StatusMensalidade.PAGA) {
            mensalidade.pagar();
        } else {
            mensalidade.marcarComoPendente();
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
        return toDTO(salvo);
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        var assinatura = usuario.getAssinatura();
        var mensalidade = usuario.getMensalidade();
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                assinatura != null ? assinatura.getPlano().name() : null,
                assinatura != null ? assinatura.getCreditosCursos() : null,
                assinatura != null ? assinatura.getCursosConcluidosComSucesso() : null,
                assinatura != null ? assinatura.getMoedas() : null,
                mensalidade != null ? mensalidade.getStatus().name() : null,
                usuario.temAcessoAoCurso(),
                usuario.isPlataformaCongelada()
        );
    }
}

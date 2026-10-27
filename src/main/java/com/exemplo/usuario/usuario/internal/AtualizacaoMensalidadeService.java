package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE.
// Caso de uso de atualizacao da mensalidade de um usuario.
// A mensalidade e gravada pelo modulo mensalidade; aqui reavaliamos as flags de acesso.
@Service
public class AtualizacaoMensalidadeService {

    private final UsuarioRepository usuarioRepository;
    private final MensalidadeService mensalidadeService;
    private final UsuarioResponseMapper mapper;

    public AtualizacaoMensalidadeService(UsuarioRepository usuarioRepository,
                                         MensalidadeService mensalidadeService,
                                         UsuarioResponseMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.mensalidadeService = mensalidadeService;
        this.mapper = mapper;
    }

    @Transactional
    public UsuarioResponseDTO atualizarStatus(Long usuarioId, MensalidadeRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(RecursoNaoEncontradoException.com("Usuario nao encontrado"));

        // trim + maiusculas; valor invalido -> IllegalArgumentException (400).
        StatusMensalidade status = StatusMensalidade.de(dto.getStatus());
        mensalidadeService.definirStatus(usuarioId, status);

        // Reavalia o acesso a plataforma imediatamente apos a mudanca de status.
        // Se ficar bloqueado, a excecao e apenas o sinal de que o estado foi
        // atualizado; o usuario continua sendo persistido com as flags corretas.
        try {
            usuario.validarAcessoPlataforma(mensalidadeService.estaPaga(usuarioId));
        } catch (IllegalStateException ignored) {
            // Estado ja registrado em usuario (acessoAoCurso=false, plataformaCongelada=true).
        }

        Usuario salvo = usuarioRepository.save(usuario);
        return mapper.de(salvo);
    }
}

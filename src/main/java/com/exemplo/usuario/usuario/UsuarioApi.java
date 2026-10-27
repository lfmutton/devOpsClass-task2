package com.exemplo.usuario.usuario;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.internal.Usuario;
import com.exemplo.usuario.usuario.internal.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// API PUBLICA do modulo usuario.
// Outros modulos (ex.: matricula) so conversam com o usuario por aqui,
// sem enxergar a entidade Usuario nem o repository (que ficam em ".internal").
@Service
public class UsuarioApi {

    private static final String USUARIO_NAO_ENCONTRADO = "Usuario nao encontrado";

    private final UsuarioRepository repository;
    private final MensalidadeService mensalidadeService;

    public UsuarioApi(UsuarioRepository repository, MensalidadeService mensalidadeService) {
        this.repository = repository;
        this.mensalidadeService = mensalidadeService;
    }

    // Reavalia o acesso do usuario a plataforma a partir da mensalidade.
    // Mensalidade nao paga (ou inexistente) bloqueia e lanca IllegalStateException.
    // O "finally" garante que as flags atualizadas sejam salvas mesmo no bloqueio.
    @Transactional
    public void validarAcessoPlataforma(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        try {
            usuario.validarAcessoPlataforma(mensalidadeService.estaPaga(usuarioId));
        } finally {
            repository.save(usuario);
        }
    }

    public String buscarNome(Long usuarioId) {
        return buscar(usuarioId).getPerfil().getNome();
    }

    private Usuario buscar(Long usuarioId) {
        return repository.findById(usuarioId)
                .orElseThrow(RecursoNaoEncontradoException.com(USUARIO_NAO_ENCONTRADO));
    }
}

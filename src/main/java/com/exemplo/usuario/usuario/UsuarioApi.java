package com.exemplo.usuario.usuario;

import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.usuario.internal.domain.Usuario;
import com.exemplo.usuario.usuario.internal.repository.UsuarioRepository;
import com.exemplo.usuario.usuario.internal.service.UsuarioResponseMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// API PUBLICA do modulo usuario.
// Outros modulos (ex.: matricula, mensalidade) so conversam com o usuario por aqui,
// sem enxergar a entidade Usuario nem o repository (que ficam em ".internal").
@Service
public class UsuarioApi {

    private static final String USUARIO_NAO_ENCONTRADO = "Usuario nao encontrado";

    private final UsuarioRepository repository;
    private final MensalidadeConsulta mensalidadeConsulta;
    private final UsuarioResponseMapper mapper;

    public UsuarioApi(UsuarioRepository repository, MensalidadeConsulta mensalidadeConsulta,
                      UsuarioResponseMapper mapper) {
        this.repository = repository;
        this.mensalidadeConsulta = mensalidadeConsulta;
        this.mapper = mapper;
    }

    // Reavalia o acesso do usuario a plataforma a partir da mensalidade.
    // Mensalidade nao paga (ou inexistente) bloqueia e lanca IllegalStateException.
    // O "finally" garante que as flags atualizadas sejam salvas mesmo no bloqueio.
    @Transactional
    public void validarAcessoPlataforma(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        try {
            usuario.validarAcessoPlataforma(mensalidadeConsulta.estaPaga(usuarioId));
        } finally {
            repository.save(usuario);
        }
    }

    // Reavalia o acesso depois de uma mudanca na mensalidade e devolve o usuario atualizado.
    // Aqui o bloqueio NAO e erro: a excecao so sinaliza que as flags foram atualizadas.
    @Transactional
    public UsuarioResponseDTO reavaliarAcesso(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        try {
            usuario.validarAcessoPlataforma(mensalidadeConsulta.estaPaga(usuarioId));
        } catch (IllegalStateException ignored) {
            // Estado ja registrado no usuario (acessoAoCurso=false, plataformaCongelada=true).
        }
        return mapper.de(repository.save(usuario));
    }

    // Lanca RecursoNaoEncontradoException se o usuario nao existir.
    public void verificarExistencia(Long usuarioId) {
        buscar(usuarioId);
    }

    public String buscarNome(Long usuarioId) {
        return buscar(usuarioId).getPerfil().getNome();
    }

    private Usuario buscar(Long usuarioId) {
        return repository.findById(usuarioId)
                .orElseThrow(RecursoNaoEncontradoException.com(USUARIO_NAO_ENCONTRADO));
    }
}

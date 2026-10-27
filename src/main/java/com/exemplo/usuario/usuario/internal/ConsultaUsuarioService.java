package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada: SERVICE.
// Casos de uso de LEITURA de usuarios (listar e buscar por id).
@Service
@Transactional(readOnly = true)
public class ConsultaUsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioResponseMapper mapper;

    public ConsultaUsuarioService(UsuarioRepository repository, UsuarioResponseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream().map(mapper::de).toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(RecursoNaoEncontradoException.com("Usuario nao encontrado"));
        return mapper.de(usuario);
    }
}

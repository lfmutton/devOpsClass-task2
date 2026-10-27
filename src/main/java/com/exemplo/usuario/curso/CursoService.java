package com.exemplo.usuario.curso;

import com.exemplo.usuario.curso.internal.Curso;
import com.exemplo.usuario.curso.internal.CursoRepository;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada: SERVICE (API publica do modulo curso).
// Service concentra regras de negocio e orquestracao do caso de uso.
// Outros modulos (ex.: matricula) conversam com o modulo curso apenas por aqui.
@Service
public class CursoService {

    private final CursoRepository repository;

    // Injecao de dependencia por construtor.
    // O bean de CursoRepository e entregue pelo Spring.
    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }

    public List<CursoResponseDTO> listarTodos() {
        // findAll() vem do JpaRepository.
        // map(this::toDTO) converte entidade em DTO de saida.
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    // @Transactional e tipico da camada de servico.
    // Indica que o metodo deve executar dentro de uma transacao.
    @Transactional
    public CursoResponseDTO criar(CursoRequestDTO dto) {
        // Aqui o service instancia a entidade de dominio.
        Curso curso = new Curso(dto.getTitulo(), dto.getDescricao());

        // save(...) persiste no banco.
        return toDTO(repository.save(curso));
    }

    // Busca um curso pelo id e devolve o DTO.
    // Usado por outros modulos (ex.: matricula), que nao enxergam a entidade Curso.
    public CursoResponseDTO buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(RecursoNaoEncontradoException.com("Curso nao encontrado"));
    }

    // Metodo privado de apoio para mapear entidade -> DTO.
    private CursoResponseDTO toDTO(Curso curso) {
        return new CursoResponseDTO(curso.getId(), curso.getTitulo(), curso.getDescricao());
    }
}

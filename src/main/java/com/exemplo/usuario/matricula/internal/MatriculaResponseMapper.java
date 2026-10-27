package com.exemplo.usuario.matricula.internal;

import com.exemplo.usuario.curso.CursoService;
import com.exemplo.usuario.usuario.UsuarioApi;
import org.springframework.stereotype.Component;

// Converte a entidade Matricula no DTO de resposta.
// Como a Matricula guarda apenas os ids, o nome do usuario e o titulo do curso
// sao buscados pela API publica dos modulos usuario e curso.
@Component
public class MatriculaResponseMapper {

    private final UsuarioApi usuarioApi;
    private final CursoService cursoService;

    public MatriculaResponseMapper(UsuarioApi usuarioApi, CursoService cursoService) {
        this.usuarioApi = usuarioApi;
        this.cursoService = cursoService;
    }

    public MatriculaResponseDTO paraDTO(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getId(),
                alunoCurso(matricula),
                situacao(matricula)
        );
    }

    private AlunoCursoResponseDTO alunoCurso(Matricula matricula) {
        return new AlunoCursoResponseDTO(
                matricula.getUsuarioId(),
                usuarioApi.buscarNome(matricula.getUsuarioId()),
                matricula.getCursoId(),
                cursoService.buscarPorId(matricula.getCursoId()).titulo()
        );
    }

    private SituacaoMatriculaResponseDTO situacao(Matricula matricula) {
        return new SituacaoMatriculaResponseDTO(
                matricula.getSituacao().getStatus().name(),
                matricula.getSituacao().getNotaFinal(),
                matricula.isBonus()
        );
    }
}

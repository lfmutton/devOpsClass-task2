package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Matricula;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

// DTO de saida para devolver os dados da matricula ao cliente.
// @JsonUnwrapped "achata" as partes no JSON, mantendo o formato que o frontend usa:
// { id, usuarioId, usuarioNome, cursoId, cursoTitulo, status, notaFinal, bonus }
public record MatriculaResponseDTO(
        Long id,
        @JsonUnwrapped AlunoCursoResponseDTO alunoCurso,
        @JsonUnwrapped SituacaoMatriculaResponseDTO situacao) {

    public static MatriculaResponseDTO de(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getId(),
                AlunoCursoResponseDTO.de(matricula),
                SituacaoMatriculaResponseDTO.de(matricula)
        );
    }
}

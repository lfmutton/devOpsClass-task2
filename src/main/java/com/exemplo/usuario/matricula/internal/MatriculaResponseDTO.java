package com.exemplo.usuario.matricula.internal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

// DTO de saida para devolver os dados da matricula ao cliente.
// @JsonUnwrapped "achata" as partes no JSON, mantendo o formato que o frontend usa:
// { id, usuarioId, usuarioNome, cursoId, cursoTitulo, status, notaFinal, bonus }
// A montagem fica no MatriculaResponseMapper, que consulta os outros modulos.
public record MatriculaResponseDTO(
        Long id,
        @JsonUnwrapped AlunoCursoResponseDTO alunoCurso,
        @JsonUnwrapped SituacaoMatriculaResponseDTO situacao) {
}

package com.exemplo.usuario.matricula.internal;

// Parte do MatriculaResponseDTO com quem esta matriculado e em qual curso.
// O nome do usuario e o titulo do curso vem dos modulos usuario e curso
// (montados pelo MatriculaResponseMapper).
public record AlunoCursoResponseDTO(
        Long usuarioId,
        String usuarioNome,
        Long cursoId,
        String cursoTitulo) {
}

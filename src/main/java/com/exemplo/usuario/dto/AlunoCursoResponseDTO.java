package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Matricula;

// Parte do MatriculaResponseDTO com quem esta matriculado e em qual curso.
public record AlunoCursoResponseDTO(
        Long usuarioId,
        String usuarioNome,
        Long cursoId,
        String cursoTitulo) {

    public static AlunoCursoResponseDTO de(Matricula matricula) {
        return new AlunoCursoResponseDTO(
                matricula.getUsuario().getId(),
                matricula.getUsuario().getPerfil().getNome(),
                matricula.getCurso().getId(),
                matricula.getCurso().getTitulo()
        );
    }
}

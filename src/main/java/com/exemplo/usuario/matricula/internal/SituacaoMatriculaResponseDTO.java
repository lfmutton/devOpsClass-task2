package com.exemplo.usuario.matricula.internal;

// Parte do MatriculaResponseDTO com o estado da matricula.
public record SituacaoMatriculaResponseDTO(
        String status,
        Double notaFinal,
        boolean bonus) {
}

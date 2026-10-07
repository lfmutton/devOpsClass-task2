package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Matricula;

// Parte do MatriculaResponseDTO com o estado da matricula.
public record SituacaoMatriculaResponseDTO(
        String status,
        Double notaFinal,
        boolean bonus) {

    public static SituacaoMatriculaResponseDTO de(Matricula matricula) {
        return new SituacaoMatriculaResponseDTO(
                matricula.getSituacao().getStatus().name(),
                matricula.getSituacao().getNotaFinal(),
                matricula.isBonus()
        );
    }
}

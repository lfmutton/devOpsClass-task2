package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.AcessoPlataforma;
import com.exemplo.usuario.domain.Mensalidade;

// Parte do UsuarioResponseDTO com a mensalidade e o acesso a plataforma.
public record AcessoResponseDTO(
        String statusMensalidade,
        boolean temAcessoAoCurso,
        boolean plataformaCongelada) {

    public static AcessoResponseDTO de(AcessoPlataforma acesso) {
        Mensalidade mensalidade = acesso.getMensalidade();
        return new AcessoResponseDTO(
                mensalidade != null ? mensalidade.getStatus().name() : null,
                acesso.temAcessoAoCurso(),
                acesso.isPlataformaCongelada()
        );
    }
}

package com.exemplo.usuario.usuario.internal;

// Parte do UsuarioResponseDTO com a mensalidade e o acesso a plataforma.
// statusMensalidade vem do modulo mensalidade; as flags vem da tabela usuarios.
public record AcessoResponseDTO(
        String statusMensalidade,
        boolean temAcessoAoCurso,
        boolean plataformaCongelada) {
}

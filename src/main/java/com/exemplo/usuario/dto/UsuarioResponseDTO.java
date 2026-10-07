package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

// DTO de saida da API para usuario.
// Repare que ele nao expõe a senha.
// @JsonUnwrapped "achata" as partes no JSON, mantendo o formato que o frontend usa:
// { id, nome, email, plano, creditosCursos, ..., statusMensalidade, temAcessoAoCurso, plataformaCongelada }
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        @JsonUnwrapped AssinaturaResponseDTO assinatura,
        @JsonUnwrapped AcessoResponseDTO acesso) {

    // Centraliza o mapeamento entidade -> DTO (antes duplicado em dois services).
    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getPerfil().getNome(),
                usuario.getPerfil().getEmail(),
                AssinaturaResponseDTO.de(usuario.getAssinatura()),
                AcessoResponseDTO.de(usuario.getAcesso())
        );
    }
}

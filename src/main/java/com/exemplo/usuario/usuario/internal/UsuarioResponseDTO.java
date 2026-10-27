package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

// DTO de saida da API para usuario.
// Repare que ele nao expoe a senha.
// @JsonUnwrapped "achata" as partes no JSON, mantendo o formato que o frontend usa:
// { id, nome, email, plano, creditosCursos, ..., statusMensalidade, temAcessoAoCurso, plataformaCongelada }
// A montagem (que consulta outros modulos) fica em UsuarioResponseMapper.
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        @JsonUnwrapped AssinaturaResponseDTO assinatura,
        @JsonUnwrapped AcessoResponseDTO acesso) {
}

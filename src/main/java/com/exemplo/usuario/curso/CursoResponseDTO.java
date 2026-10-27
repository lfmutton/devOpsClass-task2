package com.exemplo.usuario.curso;

// DTO de saida da API para curso.
// Record gera construtor, accessors (id(), titulo(), descricao()), equals, hashCode e toString.
// O Jackson serializa os componentes do record com os mesmos nomes: {"id", "titulo", "descricao"}.
public record CursoResponseDTO(Long id, String titulo, String descricao) {
}

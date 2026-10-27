package com.exemplo.usuario.assinatura;

// Parte do UsuarioResponseDTO com os dados da assinatura.
// O modulo usuario usa este record com @JsonUnwrapped,
// entao os nomes dos componentes viram as chaves do JSON e NAO podem mudar.
// Usuario sem assinatura (dados antigos) gera todos os campos nulos.
public record AssinaturaResponseDTO(
        String plano,
        Integer creditosCursos,
        Integer cursosConcluidosComSucesso,
        Integer moedas) {
}

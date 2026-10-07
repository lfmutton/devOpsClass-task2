package com.exemplo.usuario.domain.vo;

// Apoio aos Value Objects de texto.
// Centraliza a validacao/normalizacao que antes era repetida em cada VO.
final class Texto {

    private Texto() {
    }

    // Texto obrigatorio: rejeita null/vazio/so espacos e devolve sem espacos nas pontas.
    static String obrigatorio(String valor, String mensagemErro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return valor.trim();
    }

    // Texto opcional: aceita null, mas remove espacos nas pontas quando houver valor.
    static String opcional(String valor) {
        return valor == null ? null : valor.trim();
    }
}

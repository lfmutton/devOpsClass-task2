package com.exemplo.usuario.shared;

// Camada: SHARED (modulo compartilhado).
// Apoio aos Value Objects de texto de todos os modulos.
// Centraliza a validacao/normalizacao que antes era repetida em cada VO.
// E publica porque cada modulo tem seus proprios VOs (nome, email, titulo...).
public final class Texto {

    private Texto() {
    }

    // Texto obrigatorio: rejeita null/vazio/so espacos e devolve sem espacos nas pontas.
    public static String obrigatorio(String valor, String mensagemErro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return valor.trim();
    }

    // Texto opcional: aceita null, mas remove espacos nas pontas quando houver valor.
    public static String opcional(String valor) {
        return valor == null ? null : valor.trim();
    }
}

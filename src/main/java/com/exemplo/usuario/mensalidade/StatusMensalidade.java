package com.exemplo.usuario.mensalidade;

// API PUBLICA do modulo mensalidade.
// Enum com os estados possiveis da mensalidade do usuario.
public enum StatusMensalidade {
    PENDENTE,
    PAGA;

    // Converte um texto vindo de fora (ex.: corpo da requisicao) para o enum.
    // Ignora espacos nas pontas e diferenca de maiusculas/minusculas.
    // Texto invalido lanca IllegalArgumentException (comportamento natural do valueOf).
    public static StatusMensalidade de(String texto) {
        return valueOf(texto.trim().toUpperCase());
    }
}

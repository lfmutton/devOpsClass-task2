package com.exemplo.usuario.usuario;

// Evento publicado quando um usuario e cadastrado.
// Outros modulos reagem a ele sem que o usuario precise conhece-los
// (ex.: o modulo mensalidade cria a mensalidade PAGA inicial).
public record UsuarioCriadoEvent(Long usuarioId) {
}

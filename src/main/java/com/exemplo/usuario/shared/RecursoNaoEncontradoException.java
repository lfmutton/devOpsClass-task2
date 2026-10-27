package com.exemplo.usuario.shared;

import java.util.function.Supplier;

// Camada: SHARED (modulo compartilhado).
// Excecao para quando um registro buscado por id nao existe.
// Continua sendo RuntimeException, entao o ApiExceptionHandler a trata como antes.
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    // Uso: repository.findById(id).orElseThrow(RecursoNaoEncontradoException.com("Usuario nao encontrado"))
    public static Supplier<RecursoNaoEncontradoException> com(String mensagem) {
        return () -> new RecursoNaoEncontradoException(mensagem);
    }
}

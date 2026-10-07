package com.exemplo.usuario.domain.vo;

import java.util.regex.Pattern;

// Regra de formato do e-mail, separada do Value Object EmailUsuario.
final class FormatoEmail {

    // Pattern = expressao regular reutilizavel para validar formato do e-mail.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private FormatoEmail() {
    }

    static String validar(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("E-mail invalido");
        }
        return email;
    }
}

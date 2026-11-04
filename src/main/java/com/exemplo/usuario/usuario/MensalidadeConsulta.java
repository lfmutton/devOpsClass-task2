package com.exemplo.usuario.usuario;

import java.util.Optional;

// Porta (interface) que o modulo usuario PRECISA, mas nao implementa.
// Quem implementa e o modulo mensalidade (inversao de dependencia):
// assim o usuario nao depende do modulo mensalidade, e sim o contrario.
public interface MensalidadeConsulta {

    // Usuario sem mensalidade e tratado como "nao paga".
    boolean estaPaga(Long usuarioId);

    // Nome do status ("PAGA"/"PENDENTE"), ou vazio se o usuario nao tem mensalidade.
    Optional<String> statusAtual(Long usuarioId);
}

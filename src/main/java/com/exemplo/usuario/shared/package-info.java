// Modulo: SHARED.
// Codigo comum a todos os modulos do monolito modular:
// - RecursoNaoEncontradoException e Texto (API publica, usada pelos outros modulos);
// - config: seguranca (HTTP Basic + PasswordEncoder) e documentacao OpenAPI;
// - web: tratamento global de erros (ApiExceptionHandler) e a pagina /home.
// Regra: shared NAO depende de nenhum outro modulo.
package com.exemplo.usuario.shared;

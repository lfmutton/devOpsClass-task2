// Modulo: MATRICULA.
// Liga um usuario a um curso e controla o ciclo de vida da matricula (status e nota).
// Depende de: usuario (UsuarioApi), curso (CursoService), assinatura (AssinaturaService) e shared.
// Nenhum outro modulo depende de matricula, por isso este pacote base nao expoe API publica:
// todo o codigo fica em .internal (privado do modulo).
package com.exemplo.usuario.matricula;

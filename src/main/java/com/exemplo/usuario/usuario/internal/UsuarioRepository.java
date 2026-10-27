package com.exemplo.usuario.usuario.internal;

import org.springframework.data.jpa.repository.JpaRepository;

// Repository de Usuario (privado do modulo usuario).
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // EmailUsuario e um Value Object embutido dentro de PerfilUsuario,
    // entao o Spring Data navega ate perfil.email.valor.
    boolean existsByPerfilEmailValor(String email);
}

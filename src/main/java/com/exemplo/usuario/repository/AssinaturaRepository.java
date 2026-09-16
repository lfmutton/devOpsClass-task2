package com.exemplo.usuario.repository;

import com.exemplo.usuario.domain.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Camada: REPOSITORY.
// Repository conversa com a persistencia.
// Quase sempre ela trabalha com entidades JPA.
public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    // Metodo derivado por nome.
    // O Spring Data gera a implementacao automaticamente.
    Optional<Assinatura> findByUsuarioId(Long usuarioId);
}

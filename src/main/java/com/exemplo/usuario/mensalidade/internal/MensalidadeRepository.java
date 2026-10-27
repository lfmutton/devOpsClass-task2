package com.exemplo.usuario.mensalidade.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Camada: REPOSITORY (privado do modulo mensalidade).
public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {

    Optional<Mensalidade> findByUsuarioId(Long usuarioId);
}

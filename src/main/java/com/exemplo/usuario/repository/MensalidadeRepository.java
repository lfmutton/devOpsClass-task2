package com.exemplo.usuario.repository;

import com.exemplo.usuario.domain.Mensalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Camada: REPOSITORY.
// Repository de Mensalidade, no mesmo padrao usado para Assinatura.
public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {

    Optional<Mensalidade> findByUsuarioId(Long usuarioId);
}

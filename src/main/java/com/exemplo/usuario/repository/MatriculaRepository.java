package com.exemplo.usuario.repository;

import com.exemplo.usuario.domain.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository de Matricula.
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    // O Spring Data entende "findByUsuarioId" e monta a consulta.
    List<Matricula> findByUsuarioId(Long usuarioId);
}

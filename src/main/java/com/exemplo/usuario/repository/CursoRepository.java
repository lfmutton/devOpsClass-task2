package com.exemplo.usuario.repository;

import com.exemplo.usuario.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository de Curso.
// Herdamos varios metodos prontos como:
// - findAll()
// - findById(...)
// - save(...)
// - deleteById(...)
public interface CursoRepository extends JpaRepository<Curso, Long> {
}

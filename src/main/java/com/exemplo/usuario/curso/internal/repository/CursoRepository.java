package com.exemplo.usuario.curso.internal.repository;

import com.exemplo.usuario.curso.internal.domain.Curso;

import org.springframework.data.jpa.repository.JpaRepository;

// Repository de Curso (interno do modulo curso).
// E public porque o CursoService (pacote base do modulo) precisa injeta-lo.
// Herdamos varios metodos prontos como:
// - findAll()
// - findById(...)
// - save(...)
// - deleteById(...)
public interface CursoRepository extends JpaRepository<Curso, Long> {
}

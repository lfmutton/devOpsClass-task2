package com.exemplo.usuario.matricula.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository de Matricula (privado do modulo matricula).
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    // O Spring Data entende "findByUsuarioId" e monta a consulta
    // usando o atributo usuarioId (coluna usuario_id).
    List<Matricula> findByUsuarioId(Long usuarioId);
}

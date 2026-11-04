// DTOs publicos do modulo curso.
// Para o Spring Modulith, subpacotes de um modulo sao privados por padrao.
// @NamedInterface libera este pacote para os outros modulos (ex.: matricula usa CursoResponseDTO).
@NamedInterface("dto")
package com.exemplo.usuario.curso.dto;

import org.springframework.modulith.NamedInterface;

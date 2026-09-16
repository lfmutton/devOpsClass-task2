package com.exemplo.usuario.controller;

import com.exemplo.usuario.dto.ConcluirMatriculaRequestDTO;
import com.exemplo.usuario.dto.MatriculaRequestDTO;
import com.exemplo.usuario.dto.MatriculaResponseDTO;
import com.exemplo.usuario.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Camada: CONTROLLER.
// Responsavel por expor os endpoints de matricula.
@RestController
@RequestMapping("/api/matriculas")
@Tag(name = "Matriculas")
public class MatriculaRestController {

    private final MatriculaService service;

    // Injecao de dependência por construtor.
    public MatriculaRestController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Matricular usuario em um curso")
    public MatriculaResponseDTO matricular(@Valid @RequestBody MatriculaRequestDTO dto) {
        // Controller extrai os dados do DTO e repassa para a camada de servico.
        return service.matricular(dto.getUsuarioId(), dto.getCursoId(), dto.isBonus());
    }

    // @PutMapping costuma ser usado para atualizacao.
    @PutMapping("/{id}/concluir")
    @Operation(summary = "Concluir matricula com nota final")
    public MatriculaResponseDTO concluir(@PathVariable Long id,
                                         @Valid @RequestBody ConcluirMatriculaRequestDTO dto) {
        // @PathVariable pega o id diretamente da URL.
        return service.concluir(id, dto.getNotaFinal());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar matriculas por usuario")
    public List<MatriculaResponseDTO> listarPorUsuario(@PathVariable Long usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }
}

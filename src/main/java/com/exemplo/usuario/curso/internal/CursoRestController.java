package com.exemplo.usuario.curso.internal;

import com.exemplo.usuario.curso.CursoRequestDTO;
import com.exemplo.usuario.curso.CursoResponseDTO;
import com.exemplo.usuario.curso.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Camada: CONTROLLER (interna do modulo curso).
// Controller conversa com HTTP.
// Ela recebe requisicoes, chama a camada de service e devolve JSON.
// Fica no .internal porque nenhum outro modulo precisa chama-la: so o HTTP.
@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos")
public class CursoRestController {

    private final CursoService service;

    // Injecao de dependencia por construtor.
    // O Spring Container cria o CursoService e o entrega aqui.
    public CursoRestController(CursoService service) {
        this.service = service;
    }

    // Mapeia HTTP GET para listar recursos.
    @GetMapping
    @Operation(summary = "Listar cursos")
    public List<CursoResponseDTO> listar() {
        return service.listarTodos();
    }

    // Mapeia HTTP POST para criacao de recurso (201 Created).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar curso")
    public CursoResponseDTO criar(@Valid @RequestBody CursoRequestDTO dto) {
        // @RequestBody converte JSON em objeto Java.
        // @Valid executa as validacoes do DTO antes de chegar na regra de negocio.
        return service.criar(dto);
    }
}

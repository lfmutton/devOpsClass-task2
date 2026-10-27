package com.exemplo.usuario.usuario.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Camada: CONTROLLER.
// Esta camada fala com o mundo externo via HTTP.
// Leitura e criacao foram separadas em dois services para manter cada classe pequena.
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioRestController {

    private final ConsultaUsuarioService consultaService;
    private final CadastroUsuarioService cadastroService;

    // DI por construtor: o Spring injeta os beans anotados com @Service.
    public UsuarioRestController(ConsultaUsuarioService consultaService, CadastroUsuarioService cadastroService) {
        this.consultaService = consultaService;
        this.cadastroService = cadastroService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponseDTO> listar() {
        return consultaService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por id")
    public UsuarioResponseDTO buscar(@PathVariable Long id) {
        return consultaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuario com assinatura basica")
    public UsuarioResponseDTO criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return cadastroService.criar(dto);
    }
}

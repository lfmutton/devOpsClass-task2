package com.exemplo.usuario.controller;

import com.exemplo.usuario.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.service.MensalidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// Camada: CONTROLLER.
// Responsavel por expor o endpoint de atualizacao da mensalidade de um usuario.
@RestController
@RequestMapping("/api/usuarios/{usuarioId}/mensalidade")
@Tag(name = "Mensalidades")
public class MensalidadeRestController {

    private final MensalidadeService service;

    public MensalidadeRestController(MensalidadeService service) {
        this.service = service;
    }

    @PutMapping
    @Operation(summary = "Atualizar status da mensalidade do usuario (PAGA ou PENDENTE)")
    public UsuarioResponseDTO atualizarStatus(@PathVariable Long usuarioId,
                                              @Valid @RequestBody MensalidadeRequestDTO dto) {
        return service.atualizarStatus(usuarioId, dto);
    }
}

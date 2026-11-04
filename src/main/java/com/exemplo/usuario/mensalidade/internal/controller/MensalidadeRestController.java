package com.exemplo.usuario.mensalidade.internal.controller;

import com.exemplo.usuario.mensalidade.internal.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.mensalidade.internal.service.AtualizacaoMensalidadeService;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// Camada: CONTROLLER.
// Responsavel por expor o endpoint de atualizacao da mensalidade de um usuario.
// A URL continua sob /api/usuarios/{id} (o frontend usa esse caminho),
// mas o endpoint pertence ao modulo mensalidade.
@RestController
@RequestMapping("/api/usuarios/{usuarioId}/mensalidade")
@Tag(name = "Mensalidades")
public class MensalidadeRestController {

    private final AtualizacaoMensalidadeService service;

    public MensalidadeRestController(AtualizacaoMensalidadeService service) {
        this.service = service;
    }

    @PutMapping
    @Operation(summary = "Atualizar status da mensalidade do usuario (PAGA ou PENDENTE)")
    public UsuarioResponseDTO atualizarStatus(@PathVariable Long usuarioId,
                                              @Valid @RequestBody MensalidadeRequestDTO dto) {
        return service.atualizarStatus(usuarioId, dto);
    }
}

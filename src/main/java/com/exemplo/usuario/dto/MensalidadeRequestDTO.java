package com.exemplo.usuario.dto;

import jakarta.validation.constraints.NotBlank;

// DTO de entrada para atualizar o status da mensalidade de um usuario.
// Aceita "PENDENTE" ou "PAGA".
public class MensalidadeRequestDTO {

    @NotBlank(message = "status e obrigatorio")
    private String status;

    public MensalidadeRequestDTO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

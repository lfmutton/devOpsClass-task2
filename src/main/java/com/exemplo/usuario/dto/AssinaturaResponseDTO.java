package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Assinatura;

// Parte do UsuarioResponseDTO com os dados da assinatura.
public record AssinaturaResponseDTO(
        String plano,
        Integer creditosCursos,
        Integer cursosConcluidosComSucesso,
        Integer moedas) {

    // Usuario sem assinatura (dados antigos) gera todos os campos nulos.
    public static AssinaturaResponseDTO de(Assinatura assinatura) {
        if (assinatura == null) {
            return new AssinaturaResponseDTO(null, null, null, null);
        }
        return new AssinaturaResponseDTO(
                assinatura.getProgresso().getPlano().name(),
                assinatura.getCarteira().getCreditosCursos(),
                assinatura.getProgresso().getCursosConcluidosComSucesso(),
                assinatura.getCarteira().getMoedas()
        );
    }
}

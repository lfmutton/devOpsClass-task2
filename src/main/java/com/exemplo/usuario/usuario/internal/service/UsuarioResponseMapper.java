package com.exemplo.usuario.usuario.internal.service;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.usuario.MensalidadeConsulta;
import com.exemplo.usuario.usuario.dto.AcessoResponseDTO;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.usuario.internal.domain.Usuario;

import org.springframework.stereotype.Component;

// Monta o UsuarioResponseDTO juntando informacoes de tres modulos:
// - usuario: id, nome, e-mail e flags de acesso
// - assinatura: plano, creditos, cursos concluidos e moedas
// - mensalidade (via MensalidadeConsulta): status da mensalidade (ou null se nao existir)
@Component
public class UsuarioResponseMapper {

    private final AssinaturaService assinaturaService;
    private final MensalidadeConsulta mensalidadeConsulta;

    public UsuarioResponseMapper(AssinaturaService assinaturaService, MensalidadeConsulta mensalidadeConsulta) {
        this.assinaturaService = assinaturaService;
        this.mensalidadeConsulta = mensalidadeConsulta;
    }

    public UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getPerfil().getNome(),
                usuario.getPerfil().getEmail(),
                assinaturaService.resumo(usuario.getId()),
                acesso(usuario)
        );
    }

    private AcessoResponseDTO acesso(Usuario usuario) {
        String statusMensalidade = mensalidadeConsulta.statusAtual(usuario.getId()).orElse(null);
        return new AcessoResponseDTO(
                statusMensalidade,
                usuario.getAcesso().temAcessoAoCurso(),
                usuario.getAcesso().isPlataformaCongelada()
        );
    }
}

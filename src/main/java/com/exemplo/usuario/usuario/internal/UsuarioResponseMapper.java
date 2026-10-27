package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.mensalidade.MensalidadeService;
import org.springframework.stereotype.Component;

// Monta o UsuarioResponseDTO juntando informacoes de tres modulos:
// - usuario: id, nome, e-mail e flags de acesso
// - assinatura: plano, creditos, cursos concluidos e moedas
// - mensalidade: status da mensalidade (ou null se nao existir)
@Component
public class UsuarioResponseMapper {

    private final AssinaturaService assinaturaService;
    private final MensalidadeService mensalidadeService;

    public UsuarioResponseMapper(AssinaturaService assinaturaService, MensalidadeService mensalidadeService) {
        this.assinaturaService = assinaturaService;
        this.mensalidadeService = mensalidadeService;
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
        // Method reference (Enum::name) em vez de lambda: mantem a complexidade baixa.
        String statusMensalidade = mensalidadeService.buscarStatus(usuario.getId())
                .map(Enum::name)
                .orElse(null);
        return new AcessoResponseDTO(
                statusMensalidade,
                usuario.getAcesso().temAcessoAoCurso(),
                usuario.getAcesso().isPlataformaCongelada()
        );
    }
}

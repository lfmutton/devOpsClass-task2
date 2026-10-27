package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE.
// Caso de uso de CRIACAO de usuario.
// Orquestra tres modulos dentro de uma unica transacao:
// usuario (salva), assinatura (cria a BASICA) e mensalidade (cria ja PAGA).
@Service
public class CadastroUsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AssinaturaService assinaturaService;
    private final MensalidadeService mensalidadeService;
    private final UsuarioResponseMapper mapper;

    public CadastroUsuarioService(UsuarioRepository repository,
                                  PasswordEncoder passwordEncoder,
                                  AssinaturaService assinaturaService,
                                  MensalidadeService mensalidadeService,
                                  UsuarioResponseMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.assinaturaService = assinaturaService;
        this.mensalidadeService = mensalidadeService;
        this.mapper = mapper;
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        // O Value Object EmailUsuario valida e normaliza (trim + minusculas).
        String emailNormalizado = new EmailUsuario(dto.getEmail()).getValor();

        // Valida regra de unicidade antes de persistir.
        if (repository.existsByPerfilEmailValor(emailNormalizado)) {
            throw new RuntimeException("E-mail ja cadastrado");
        }

        // A senha e criptografada na camada de service (nunca no controller nem no repository).
        Usuario usuario = new Usuario(dto.getNome(), emailNormalizado, passwordEncoder.encode(dto.getSenha()));
        Usuario salvo = repository.save(usuario);
        Long id = salvo.getId();

        // Os outros modulos so precisam do id do usuario.
        assinaturaService.criarParaUsuario(id);
        mensalidadeService.definirStatus(id, StatusMensalidade.PAGA);

        // Libera o acesso desde o cadastro e grava as flags atualizadas.
        salvo.validarAcessoPlataforma(mensalidadeService.estaPaga(id));
        return mapper.de(repository.save(salvo));
    }
}

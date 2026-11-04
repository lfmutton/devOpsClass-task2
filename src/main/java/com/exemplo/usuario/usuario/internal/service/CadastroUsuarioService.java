package com.exemplo.usuario.usuario.internal.service;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.usuario.MensalidadeConsulta;
import com.exemplo.usuario.usuario.UsuarioCriadoEvent;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.usuario.internal.domain.EmailUsuario;
import com.exemplo.usuario.usuario.internal.domain.Usuario;
import com.exemplo.usuario.usuario.internal.dto.UsuarioRequestDTO;
import com.exemplo.usuario.usuario.internal.repository.UsuarioRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Camada: SERVICE.
// Caso de uso de CRIACAO de usuario, numa unica transacao:
// salva o usuario, cria a assinatura BASICA e publica UsuarioCriadoEvent.
// O modulo mensalidade escuta o evento e cria a mensalidade PAGA,
// sem que o usuario precise conhecer o modulo mensalidade.
@Service
public class CadastroUsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AssinaturaService assinaturaService;
    private final MensalidadeConsulta mensalidadeConsulta;
    private final ApplicationEventPublisher eventos;
    private final UsuarioResponseMapper mapper;

    public CadastroUsuarioService(UsuarioRepository repository,
                                  PasswordEncoder passwordEncoder,
                                  AssinaturaService assinaturaService,
                                  MensalidadeConsulta mensalidadeConsulta,
                                  ApplicationEventPublisher eventos,
                                  UsuarioResponseMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.assinaturaService = assinaturaService;
        this.mensalidadeConsulta = mensalidadeConsulta;
        this.eventos = eventos;
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
        // O listener do modulo mensalidade roda aqui mesmo, na mesma transacao.
        eventos.publishEvent(new UsuarioCriadoEvent(id));

        // Libera o acesso desde o cadastro e grava as flags atualizadas.
        salvo.validarAcessoPlataforma(mensalidadeConsulta.estaPaga(id));
        return mapper.de(repository.save(salvo));
    }
}

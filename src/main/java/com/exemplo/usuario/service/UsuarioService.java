package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.domain.vo.EmailUsuario;
import com.exemplo.usuario.dto.UsuarioRequestDTO;
import com.exemplo.usuario.dto.UsuarioResponseDTO;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada: SERVICE.
// Aqui ficam as regras de negocio do caso de uso de usuario.
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    // Duas dependencias sao injetadas aqui:
    // 1) UsuarioRepository -> bean criado pelo Spring Data
    // 2) PasswordEncoder -> bean declarado em SecurityConfig
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream().map(UsuarioResponseDTO::de).toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(RecursoNaoEncontradoException.com("Usuario nao encontrado"));
        return UsuarioResponseDTO.de(usuario);
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        // A camada service costuma preparar/normalizar dados para a regra de negocio.
        // O Value Object EmailUsuario valida e normaliza (trim + minusculas).
        String emailNormalizado = new EmailUsuario(dto.getEmail()).getValor();

        // Valida regra de unicidade antes de persistir.
        if (repository.existsByPerfilEmailValor(emailNormalizado)) {
            throw new RuntimeException("E-mail ja cadastrado");
        }

        // A senha e criptografada na camada de service.
        // Isso e importante didaticamente: o controller nao deve criptografar,
        // e o repository nao deve conter regra de negocio.
        Usuario usuario = new Usuario(
                dto.getNome(),
                emailNormalizado,
                passwordEncoder.encode(dto.getSenha())
        );
        // A assinatura padrao (BASICO) ja e criada pelo construtor de Usuario.

        // Cria a mensalidade ja paga e vincula ao usuario, liberando o acesso
        // a plataforma desde o cadastro.
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        usuario.validarAcessoPlataforma();

        Usuario salvo = repository.save(usuario);
        return UsuarioResponseDTO.de(salvo);
    }
}

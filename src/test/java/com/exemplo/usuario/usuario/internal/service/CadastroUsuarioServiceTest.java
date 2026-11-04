package com.exemplo.usuario.usuario.internal.service;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.assinatura.dto.AssinaturaResponseDTO;
import com.exemplo.usuario.usuario.MensalidadeConsulta;
import com.exemplo.usuario.usuario.UsuarioCriadoEvent;
import com.exemplo.usuario.usuario.internal.domain.Usuario;
import com.exemplo.usuario.usuario.internal.dto.UsuarioRequestDTO;
import com.exemplo.usuario.usuario.internal.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroUsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private MensalidadeConsulta mensalidadeConsulta;

    @Mock
    private ApplicationEventPublisher eventos;

    private CadastroUsuarioService service;

    @BeforeEach
    void setUp() {
        var mapper = new UsuarioResponseMapper(assinaturaService, mensalidadeConsulta);
        service = new CadastroUsuarioService(repository, passwordEncoder, assinaturaService,
                mensalidadeConsulta, eventos, mapper);
    }

    private UsuarioRequestDTO novoRequest(String email) {
        var dto = new UsuarioRequestDTO();
        dto.setNome("Fulano");
        dto.setEmail(email);
        dto.setSenha("senha123");
        return dto;
    }

    // Simula o banco gerando o id na hora de salvar.
    private Usuario salvarComId(Usuario usuario) {
        ReflectionTestUtils.setField(usuario, "id", 1L);
        return usuario;
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaAssinaturaEMensalidadePaga() {
        // DADO
        when(repository.existsByPerfilEmailValor("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> salvarComId(inv.getArgument(0)));
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(true);
        when(assinaturaService.resumo(1L)).thenReturn(new AssinaturaResponseDTO("BASICO", 0, 0, 0));
        when(mensalidadeConsulta.statusAtual(1L)).thenReturn(Optional.of("PAGA"));

        // QUANDO
        var resultado = service.criar(novoRequest("  Fulano@Teste.COM "));

        // ENTAO
        // Primeiro salva o usuario (para ter id), depois cria a assinatura e
        // publica o evento que o modulo mensalidade usa para criar a mensalidade PAGA.
        InOrder ordem = inOrder(repository, assinaturaService, eventos, mensalidadeConsulta);
        ordem.verify(repository).save(any(Usuario.class));
        ordem.verify(assinaturaService).criarParaUsuario(1L);
        ordem.verify(eventos).publishEvent(new UsuarioCriadoEvent(1L));
        ordem.verify(mensalidadeConsulta).estaPaga(1L);

        assertEquals(1L, resultado.id());
        assertEquals("fulano@teste.com", resultado.email());
        assertEquals("BASICO", resultado.assinatura().plano());
        assertEquals(0, resultado.assinatura().creditosCursos());
        assertEquals(0, resultado.assinatura().cursosConcluidosComSucesso());
        assertEquals(0, resultado.assinatura().moedas());
        assertEquals("PAGA", resultado.acesso().statusMensalidade());
        assertTrue(resultado.acesso().temAcessoAoCurso());
        assertFalse(resultado.acesso().plataformaCongelada());
    }

    @Test
    void deveGuardarSenhaCriptografadaEEmailNormalizado() {
        // DADO
        when(repository.existsByPerfilEmailValor("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        var capturado = new Usuario[1];
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            capturado[0] = salvarComId(inv.getArgument(0));
            return capturado[0];
        });
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(true);
        when(mensalidadeConsulta.statusAtual(1L)).thenReturn(Optional.of("PAGA"));

        // QUANDO
        service.criar(novoRequest("FULANO@teste.com"));

        // ENTAO
        assertEquals("hash-bcrypt", capturado[0].getPerfil().getSenha());
        assertEquals("fulano@teste.com", capturado[0].getPerfil().getEmail());
        assertTrue(capturado[0].getAcesso().temAcessoAoCurso());
    }

    @Test
    void naoDeveCriarUsuarioComEmailJaCadastrado() {
        // DADO
        when(repository.existsByPerfilEmailValor("fulano@teste.com")).thenReturn(true);

        // QUANDO / ENTAO
        var ex = assertThrows(RuntimeException.class, () -> service.criar(novoRequest("FULANO@teste.com")));
        assertEquals("E-mail ja cadastrado", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any());
        verifyNoInteractions(assinaturaService, mensalidadeConsulta, eventos);
    }

    @Test
    void naoDeveCriarUsuarioSemEmail() {
        // QUANDO / ENTAO
        // O Value Object barra o e-mail nulo antes de qualquer acesso ao banco.
        var ex = assertThrows(IllegalArgumentException.class, () -> service.criar(novoRequest(null)));
        assertEquals("E-mail e obrigatorio", ex.getMessage());
        verifyNoInteractions(repository, passwordEncoder, assinaturaService, mensalidadeConsulta, eventos);
    }
}

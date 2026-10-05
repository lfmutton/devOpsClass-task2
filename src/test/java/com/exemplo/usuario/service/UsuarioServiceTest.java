package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Assinatura;
import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.dto.UsuarioRequestDTO;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, passwordEncoder);
    }

    private UsuarioRequestDTO novoRequest(String email) {
        var dto = new UsuarioRequestDTO();
        dto.setNome("Fulano");
        dto.setEmail(email);
        dto.setSenha("senha123");
        return dto;
    }

    @Test
    void deveListarTodosOsUsuarios() {
        // DADO
        // Um usuario completo e outro sem assinatura/mensalidade, para cobrir
        // os dois lados dos ternarios do toDTO.
        var completo = new Usuario("Fulano", "fulano@teste.com", "hash");
        completo.vincularAssinatura(new Assinatura(completo));
        completo.vincularMensalidade(new Mensalidade(completo, StatusMensalidade.PAGA));
        var semVinculos = new Usuario("Ciclano", "ciclano@teste.com", "hash");
        when(repository.findAll()).thenReturn(List.of(completo, semVinculos));

        // QUANDO
        var resultado = service.listarTodos();

        // ENTAO
        assertEquals(2, resultado.size());
        assertEquals("BASICO", resultado.get(0).getPlano());
        assertEquals(0, resultado.get(0).getCreditosCursos());
        assertEquals("PAGA", resultado.get(0).getStatusMensalidade());
        assertNull(resultado.get(1).getPlano());
        assertNull(resultado.get(1).getCreditosCursos());
        assertNull(resultado.get(1).getCursosConcluidosComSucesso());
        assertNull(resultado.get(1).getMoedas());
        assertNull(resultado.get(1).getStatusMensalidade());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO
        var resultado = service.buscarPorId(1L);

        // ENTAO
        assertEquals("Fulano", resultado.getNome());
        assertEquals("fulano@teste.com", resultado.getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // DADO
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaAssinaturaEMensalidadePaga() {
        // DADO
        when(repository.existsByEmailValor("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.criar(novoRequest("  Fulano@Teste.COM "));

        // ENTAO
        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        var salvo = captor.getValue();

        assertEquals("hash-bcrypt", salvo.getSenha());
        assertEquals("fulano@teste.com", salvo.getEmail());
        assertSame(salvo, salvo.getAssinatura().getUsuario());
        assertSame(salvo, salvo.getMensalidade().getUsuario());

        assertEquals("fulano@teste.com", resultado.getEmail());
        assertEquals("BASICO", resultado.getPlano());
        assertEquals(0, resultado.getCreditosCursos());
        assertEquals(0, resultado.getCursosConcluidosComSucesso());
        assertEquals(0, resultado.getMoedas());
        assertEquals("PAGA", resultado.getStatusMensalidade());
        assertTrue(resultado.isTemAcessoAoCurso());
        assertFalse(resultado.isPlataformaCongelada());
    }

    @Test
    void naoDeveCriarUsuarioComEmailJaCadastrado() {
        // DADO
        when(repository.existsByEmailValor("fulano@teste.com")).thenReturn(true);

        // QUANDO / ENTAO
        var ex = assertThrows(RuntimeException.class, () -> service.criar(novoRequest("FULANO@teste.com")));
        assertEquals("E-mail ja cadastrado", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarUsuarioSemEmail() {
        // DADO
        // E-mail nulo passa pela normalizacao e e barrado pelo Value Object.
        when(repository.existsByEmailValor(null)).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");

        // QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> service.criar(novoRequest(null)));
        verify(repository, never()).save(any());
    }
}

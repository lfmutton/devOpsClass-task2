package com.exemplo.usuario.service;

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
import static org.mockito.Mockito.verifyNoInteractions;
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
        // Um usuario com mensalidade e outro sem, para cobrir os dois lados do DTO.
        var comMensalidade = new Usuario("Fulano", "fulano@teste.com", "hash");
        comMensalidade.getAcesso().vincularMensalidade(new Mensalidade(comMensalidade, StatusMensalidade.PAGA));
        var semMensalidade = new Usuario("Ciclano", "ciclano@teste.com", "hash");
        when(repository.findAll()).thenReturn(List.of(comMensalidade, semMensalidade));

        // QUANDO
        var resultado = service.listarTodos();

        // ENTAO
        assertEquals(2, resultado.size());
        assertEquals("BASICO", resultado.get(0).assinatura().plano());
        assertEquals(0, resultado.get(0).assinatura().creditosCursos());
        assertEquals("PAGA", resultado.get(0).acesso().statusMensalidade());
        assertNull(resultado.get(1).acesso().statusMensalidade());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO
        var resultado = service.buscarPorId(1L);

        // ENTAO
        assertEquals("Fulano", resultado.nome());
        assertEquals("fulano@teste.com", resultado.email());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // DADO
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaAssinaturaEMensalidadePaga() {
        // DADO
        when(repository.existsByPerfilEmailValor("fulano@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.criar(novoRequest("  Fulano@Teste.COM "));

        // ENTAO
        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        var salvo = captor.getValue();

        assertEquals("hash-bcrypt", salvo.getPerfil().getSenha());
        assertEquals("fulano@teste.com", salvo.getPerfil().getEmail());
        assertSame(salvo, salvo.getAssinatura().getUsuario());
        assertSame(salvo, salvo.getAcesso().getMensalidade().getUsuario());

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
    void naoDeveCriarUsuarioComEmailJaCadastrado() {
        // DADO
        when(repository.existsByPerfilEmailValor("fulano@teste.com")).thenReturn(true);

        // QUANDO / ENTAO
        var ex = assertThrows(RuntimeException.class, () -> service.criar(novoRequest("FULANO@teste.com")));
        assertEquals("E-mail ja cadastrado", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarUsuarioSemEmail() {
        // QUANDO / ENTAO
        // O Value Object barra o e-mail nulo antes de qualquer acesso ao banco.
        var ex = assertThrows(IllegalArgumentException.class, () -> service.criar(novoRequest(null)));
        assertEquals("E-mail e obrigatorio", ex.getMessage());
        verifyNoInteractions(repository, passwordEncoder);
    }
}

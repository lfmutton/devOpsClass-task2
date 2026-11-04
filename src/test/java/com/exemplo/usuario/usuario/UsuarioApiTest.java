package com.exemplo.usuario.usuario;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.assinatura.dto.AssinaturaResponseDTO;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.internal.domain.Usuario;
import com.exemplo.usuario.usuario.internal.repository.UsuarioRepository;
import com.exemplo.usuario.usuario.internal.service.UsuarioResponseMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// API publica do modulo usuario (usada pelos modulos matricula e mensalidade).
@ExtendWith(MockitoExtension.class)
class UsuarioApiTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private MensalidadeConsulta mensalidadeConsulta;

    @Mock
    private AssinaturaService assinaturaService;

    private UsuarioApi api;

    @BeforeEach
    void setUp() {
        // O mapper e real: so os modulos vizinhos sao mockados.
        var mapper = new UsuarioResponseMapper(assinaturaService, mensalidadeConsulta);
        api = new UsuarioApi(repository, mensalidadeConsulta, mapper);
    }

    private Usuario usuarioExistente() {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private void usuarioNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
    }

    // Prepara o que o mapper consulta nos modulos vizinhos.
    private void vizinhosRespondem(String statusMensalidade) {
        when(mensalidadeConsulta.statusAtual(1L)).thenReturn(Optional.of(statusMensalidade));
        when(assinaturaService.resumo(1L)).thenReturn(new AssinaturaResponseDTO("BASICO", 0, 0, 0));
    }

    @Test
    void deveLiberarAcessoQuandoMensalidadeEstaPaga() {
        // DADO
        var usuario = usuarioExistente();
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(true);

        // QUANDO
        api.validarAcessoPlataforma(1L);

        // ENTAO
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
        verify(repository).save(usuario);
    }

    @Test
    void deveBloquearAcessoESalvarFlagsQuandoMensalidadeNaoEstaPaga() {
        // DADO
        var usuario = usuarioExistente();
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(false);

        // QUANDO
        var ex = assertThrows(IllegalStateException.class, () -> api.validarAcessoPlataforma(1L));

        // ENTAO
        assertEquals("Mensalidade pendente ou inexistente: acesso a plataforma bloqueado para o usuario Fulano",
                ex.getMessage());
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
        verify(repository).save(usuario);
    }

    @Test
    void deveLancar404AoValidarUsuarioInexistente() {
        // DADO
        usuarioNaoExiste();

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> api.validarAcessoPlataforma(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(mensalidadeConsulta);
    }

    @Test
    void reavaliarAcessoDeveLiberarEDevolverUsuarioAtualizado() {
        // DADO
        var usuario = usuarioExistente();
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(true);
        vizinhosRespondem("PAGA");
        when(repository.save(usuario)).thenReturn(usuario);

        // QUANDO
        var resultado = api.reavaliarAcesso(1L);

        // ENTAO
        assertEquals("PAGA", resultado.acesso().statusMensalidade());
        assertTrue(resultado.acesso().temAcessoAoCurso());
        assertFalse(resultado.acesso().plataformaCongelada());
    }

    @Test
    void reavaliarAcessoDeveCongelarSemLancarExcecao() {
        // DADO
        var usuario = usuarioExistente();
        usuario.validarAcessoPlataforma(true);
        when(mensalidadeConsulta.estaPaga(1L)).thenReturn(false);
        vizinhosRespondem("PENDENTE");
        when(repository.save(usuario)).thenReturn(usuario);

        // QUANDO
        var resultado = api.reavaliarAcesso(1L);

        // ENTAO
        // O bloqueio nao e erro aqui: o usuario e salvo e devolvido com as flags atualizadas.
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
        assertFalse(resultado.acesso().temAcessoAoCurso());
        assertTrue(resultado.acesso().plataformaCongelada());
        verify(repository).save(usuario);
    }

    @Test
    void deveVerificarExistenciaDoUsuario() {
        // DADO
        usuarioExistente();
        usuarioNaoExiste();

        // QUANDO / ENTAO
        assertDoesNotThrow(() -> api.verificarExistencia(1L));
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> api.verificarExistencia(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }

    @Test
    void deveBuscarNomeDoUsuario() {
        // DADO
        usuarioExistente();

        // QUANDO
        var nome = api.buscarNome(1L);

        // ENTAO
        assertEquals("Fulano", nome);
    }

    @Test
    void deveLancar404AoBuscarNomeDeUsuarioInexistente() {
        // DADO
        usuarioNaoExiste();

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> api.buscarNome(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }
}

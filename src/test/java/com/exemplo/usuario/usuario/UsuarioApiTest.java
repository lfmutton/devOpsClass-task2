package com.exemplo.usuario.usuario;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.internal.Usuario;
import com.exemplo.usuario.usuario.internal.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// API publica do modulo usuario (usada pelo modulo matricula).
@ExtendWith(MockitoExtension.class)
class UsuarioApiTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private MensalidadeService mensalidadeService;

    private UsuarioApi api;

    @BeforeEach
    void setUp() {
        api = new UsuarioApi(repository, mensalidadeService);
    }

    private Usuario usuarioExistente() {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    void deveLiberarAcessoQuandoMensalidadeEstaPaga() {
        // DADO
        var usuario = usuarioExistente();
        when(mensalidadeService.estaPaga(1L)).thenReturn(true);

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
        when(mensalidadeService.estaPaga(1L)).thenReturn(false);

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
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> api.validarAcessoPlataforma(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(mensalidadeService);
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
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> api.buscarNome(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }
}

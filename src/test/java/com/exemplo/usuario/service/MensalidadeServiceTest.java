package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensalidadeServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private MensalidadeService service;

    @BeforeEach
    void setUp() {
        service = new MensalidadeService(usuarioRepository);
    }

    private MensalidadeRequestDTO request(String status) {
        var dto = new MensalidadeRequestDTO();
        dto.setStatus(status);
        return dto;
    }

    private Usuario usuarioComMensalidade(StatusMensalidade status) {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, status));
        return usuario;
    }

    private void salvarDevolveOProprioUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // DADO
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizarStatus(99L, request("PAGA")));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRejeitarStatusInvalido() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> service.atualizarStatus(1L, request("ATRASADA")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveCriarMensalidadePagaQuandoUsuarioNaoTinhaMensalidade() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        salvarDevolveOProprioUsuario();

        // QUANDO
        // Status com espacos e minusculas deve ser normalizado.
        var resultado = service.atualizarStatus(1L, request("  paga "));

        // ENTAO
        assertNotNull(usuario.getAcesso().getMensalidade());
        assertSame(usuario, usuario.getAcesso().getMensalidade().getUsuario());
        assertEquals("PAGA", resultado.acesso().statusMensalidade());
        assertTrue(resultado.acesso().temAcessoAoCurso());
        assertFalse(resultado.acesso().plataformaCongelada());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveCriarMensalidadePendenteEBloquearAcessoQuandoUsuarioNaoTinhaMensalidade() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        salvarDevolveOProprioUsuario();

        // QUANDO
        var resultado = service.atualizarStatus(1L, request("PENDENTE"));

        // ENTAO
        // O bloqueio nao impede a persistencia: o usuario e salvo com as flags atualizadas.
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
        assertFalse(resultado.acesso().temAcessoAoCurso());
        assertTrue(resultado.acesso().plataformaCongelada());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void devePagarMensalidadePendenteELiberarAcesso() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PENDENTE);
        var mensalidadeOriginal = usuario.getAcesso().getMensalidade();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        salvarDevolveOProprioUsuario();

        // QUANDO
        var resultado = service.atualizarStatus(1L, request("PAGA"));

        // ENTAO
        // A mensalidade existente e reaproveitada, nao substituida.
        assertSame(mensalidadeOriginal, usuario.getAcesso().getMensalidade());
        assertTrue(mensalidadeOriginal.isPaga());
        assertEquals("PAGA", resultado.acesso().statusMensalidade());
        assertEquals("BASICO", resultado.assinatura().plano());
        assertTrue(resultado.acesso().temAcessoAoCurso());
        assertFalse(resultado.acesso().plataformaCongelada());
    }

    @Test
    void deveMarcarMensalidadePagaComoPendenteECongelarPlataforma() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        usuario.validarAcessoPlataforma();
        var mensalidadeOriginal = usuario.getAcesso().getMensalidade();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        salvarDevolveOProprioUsuario();

        // QUANDO
        var resultado = service.atualizarStatus(1L, request("pendente"));

        // ENTAO
        assertSame(mensalidadeOriginal, usuario.getAcesso().getMensalidade());
        assertEquals(StatusMensalidade.PENDENTE, mensalidadeOriginal.getStatus());
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
        assertFalse(resultado.acesso().temAcessoAoCurso());
        assertTrue(resultado.acesso().plataformaCongelada());
        verify(usuarioRepository).save(usuario);
    }
}

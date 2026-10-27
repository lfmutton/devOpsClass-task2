package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaResponseDTO;
import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

// Atualizacao da mensalidade do lado do modulo usuario:
// a mensalidade e gravada pelo MensalidadeService (mockado) e aqui
// conferimos a reavaliacao das flags de acesso e o DTO devolvido.
@ExtendWith(MockitoExtension.class)
class AtualizacaoMensalidadeServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private MensalidadeService mensalidadeService;

    private AtualizacaoMensalidadeService service;

    @BeforeEach
    void setUp() {
        var mapper = new UsuarioResponseMapper(assinaturaService, mensalidadeService);
        service = new AtualizacaoMensalidadeService(usuarioRepository, mensalidadeService, mapper);
    }

    private MensalidadeRequestDTO request(String status) {
        var dto = new MensalidadeRequestDTO();
        dto.setStatus(status);
        return dto;
    }

    private Usuario usuarioExistente() {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    // Prepara o cenario depois da gravacao: mensalidade com o status informado.
    private void mensalidadeFicou(StatusMensalidade status) {
        when(mensalidadeService.estaPaga(1L)).thenReturn(status == StatusMensalidade.PAGA);
        when(mensalidadeService.buscarStatus(1L)).thenReturn(Optional.of(status));
        when(assinaturaService.resumo(1L)).thenReturn(new AssinaturaResponseDTO("BASICO", 0, 0, 0));
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
        verifyNoInteractions(mensalidadeService);
    }

    @Test
    void deveRejeitarStatusInvalido() {
        // DADO
        usuarioExistente();

        // QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> service.atualizarStatus(1L, request("ATRASADA")));
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(mensalidadeService);
    }

    @Test
    void deveNormalizarStatusEPagarMensalidadeLiberandoAcesso() {
        // DADO
        var usuario = usuarioExistente();
        mensalidadeFicou(StatusMensalidade.PAGA);

        // QUANDO
        // Status com espacos e minusculas deve ser normalizado.
        var resultado = service.atualizarStatus(1L, request("  paga "));

        // ENTAO
        verify(mensalidadeService).definirStatus(1L, StatusMensalidade.PAGA);
        verify(usuarioRepository).save(usuario);
        assertEquals("PAGA", resultado.acesso().statusMensalidade());
        assertEquals("BASICO", resultado.assinatura().plano());
        assertTrue(resultado.acesso().temAcessoAoCurso());
        assertFalse(resultado.acesso().plataformaCongelada());
    }

    @Test
    void deveMarcarMensalidadeComoPendenteECongelarPlataforma() {
        // DADO
        var usuario = usuarioExistente();
        usuario.validarAcessoPlataforma(true);
        mensalidadeFicou(StatusMensalidade.PENDENTE);

        // QUANDO
        var resultado = service.atualizarStatus(1L, request("pendente"));

        // ENTAO
        // O bloqueio nao impede a persistencia: o usuario e salvo com as flags atualizadas.
        verify(mensalidadeService).definirStatus(1L, StatusMensalidade.PENDENTE);
        verify(usuarioRepository).save(usuario);
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
        assertFalse(resultado.acesso().temAcessoAoCurso());
        assertTrue(resultado.acesso().plataformaCongelada());
    }
}

package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.assinatura.dto.AssinaturaResponseDTO;
import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.mensalidade.internal.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.UsuarioApi;
import com.exemplo.usuario.usuario.dto.AcessoResponseDTO;
import com.exemplo.usuario.usuario.dto.UsuarioResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Atualizacao da mensalidade, agora dentro do modulo mensalidade:
// o status e gravado aqui e o modulo usuario (UsuarioApi, mockado)
// valida o usuario e reavalia o acesso.
@ExtendWith(MockitoExtension.class)
class AtualizacaoMensalidadeServiceTest {

    @Mock
    private MensalidadeService mensalidadeService;

    @Mock
    private UsuarioApi usuarioApi;

    private AtualizacaoMensalidadeService service;

    @BeforeEach
    void setUp() {
        service = new AtualizacaoMensalidadeService(mensalidadeService, usuarioApi);
    }

    private MensalidadeRequestDTO request(String status) {
        var dto = new MensalidadeRequestDTO();
        dto.setStatus(status);
        return dto;
    }

    private UsuarioResponseDTO usuarioDevolvido(String status, boolean acesso) {
        return new UsuarioResponseDTO(1L, "Fulano", "fulano@teste.com",
                new AssinaturaResponseDTO("BASICO", 0, 0, 0),
                new AcessoResponseDTO(status, acesso, !acesso));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // DADO
        doThrow(new RecursoNaoEncontradoException("Usuario nao encontrado"))
                .when(usuarioApi).verificarExistencia(99L);

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.atualizarStatus(99L, request("PAGA")));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verifyNoInteractions(mensalidadeService);
        verify(usuarioApi, never()).reavaliarAcesso(any());
    }

    @Test
    void deveRejeitarStatusInvalido() {
        // QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> service.atualizarStatus(1L, request("ATRASADA")));
        verifyNoInteractions(mensalidadeService);
        verify(usuarioApi, never()).reavaliarAcesso(any());
    }

    @Test
    void deveNormalizarStatusGravarEReavaliarAcesso() {
        // DADO
        var esperado = usuarioDevolvido("PAGA", true);
        when(usuarioApi.reavaliarAcesso(1L)).thenReturn(esperado);

        // QUANDO
        // Status com espacos e minusculas deve ser normalizado.
        var resultado = service.atualizarStatus(1L, request("  paga "));

        // ENTAO
        // Ordem: confere o usuario, grava a mensalidade e so depois reavalia o acesso.
        InOrder ordem = inOrder(usuarioApi, mensalidadeService);
        ordem.verify(usuarioApi).verificarExistencia(1L);
        ordem.verify(mensalidadeService).definirStatus(1L, StatusMensalidade.PAGA);
        ordem.verify(usuarioApi).reavaliarAcesso(1L);
        assertSame(esperado, resultado);
    }

    @Test
    void deveMarcarMensalidadeComoPendente() {
        // DADO
        when(usuarioApi.reavaliarAcesso(1L)).thenReturn(usuarioDevolvido("PENDENTE", false));

        // QUANDO
        var resultado = service.atualizarStatus(1L, request("pendente"));

        // ENTAO
        verify(mensalidadeService).definirStatus(1L, StatusMensalidade.PENDENTE);
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
        assertEquals(true, resultado.acesso().plataformaCongelada());
    }
}

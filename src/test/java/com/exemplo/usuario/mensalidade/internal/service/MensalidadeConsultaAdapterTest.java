package com.exemplo.usuario.mensalidade.internal.service;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// O adapter traduz o MensalidadeService para a porta MensalidadeConsulta do modulo usuario.
@ExtendWith(MockitoExtension.class)
class MensalidadeConsultaAdapterTest {

    @Mock
    private MensalidadeService mensalidadeService;

    private MensalidadeConsultaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MensalidadeConsultaAdapter(mensalidadeService);
    }

    @Test
    void deveRepassarSeAMensalidadeEstaPaga() {
        // DADO
        when(mensalidadeService.estaPaga(1L)).thenReturn(true);
        when(mensalidadeService.estaPaga(2L)).thenReturn(false);

        // QUANDO / ENTAO
        assertTrue(adapter.estaPaga(1L));
        assertFalse(adapter.estaPaga(2L));
    }

    @Test
    void deveDevolverONomeDoStatusOuVazio() {
        // DADO
        when(mensalidadeService.buscarStatus(1L)).thenReturn(Optional.of(StatusMensalidade.PENDENTE));
        when(mensalidadeService.buscarStatus(2L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        assertEquals(Optional.of("PENDENTE"), adapter.statusAtual(1L));
        assertEquals(Optional.empty(), adapter.statusAtual(2L));
    }
}

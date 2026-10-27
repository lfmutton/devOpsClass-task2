package com.exemplo.usuario.mensalidade;

import com.exemplo.usuario.mensalidade.internal.Mensalidade;
import com.exemplo.usuario.mensalidade.internal.MensalidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensalidadeServiceTest {

    @Mock
    private MensalidadeRepository repository;

    private MensalidadeService service;

    @BeforeEach
    void setUp() {
        service = new MensalidadeService(repository);
    }

    @Test
    void deveCriarMensalidadeQuandoUsuarioNaoTinha() {
        // DADO
        when(repository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // QUANDO
        service.definirStatus(1L, StatusMensalidade.PAGA);

        // ENTAO
        var captor = ArgumentCaptor.forClass(Mensalidade.class);
        verify(repository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUsuarioId());
        assertEquals(StatusMensalidade.PAGA, captor.getValue().getStatus());
    }

    @Test
    void deveAlterarMensalidadeExistenteSemCriarOutra() {
        // DADO
        var existente = new Mensalidade(1L, StatusMensalidade.PAGA);
        when(repository.findByUsuarioId(1L)).thenReturn(Optional.of(existente));

        // QUANDO
        service.definirStatus(1L, StatusMensalidade.PENDENTE);

        // ENTAO
        // A mesma instancia e salva: nenhuma mensalidade nova foi criada.
        var captor = ArgumentCaptor.forClass(Mensalidade.class);
        verify(repository, times(1)).save(captor.capture());
        assertSame(existente, captor.getValue());
        assertEquals(StatusMensalidade.PENDENTE, existente.getStatus());
    }

    @Test
    void deveInformarQueEstaPaga() {
        // DADO
        when(repository.findByUsuarioId(1L))
                .thenReturn(Optional.of(new Mensalidade(1L, StatusMensalidade.PAGA)));

        // QUANDO / ENTAO
        assertTrue(service.estaPaga(1L));
    }

    @Test
    void deveInformarQueNaoEstaPagaQuandoPendente() {
        // DADO
        when(repository.findByUsuarioId(1L))
                .thenReturn(Optional.of(new Mensalidade(1L, StatusMensalidade.PENDENTE)));

        // QUANDO / ENTAO
        assertFalse(service.estaPaga(1L));
    }

    @Test
    void deveInformarQueNaoEstaPagaQuandoNaoHaMensalidade() {
        // DADO
        when(repository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        assertFalse(service.estaPaga(1L));
    }

    @Test
    void deveBuscarStatusQuandoHaMensalidade() {
        // DADO
        when(repository.findByUsuarioId(1L))
                .thenReturn(Optional.of(new Mensalidade(1L, StatusMensalidade.PENDENTE)));

        // QUANDO
        var status = service.buscarStatus(1L);

        // ENTAO
        assertEquals(Optional.of(StatusMensalidade.PENDENTE), status);
    }

    @Test
    void deveDevolverVazioQuandoNaoHaMensalidade() {
        // DADO
        when(repository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // QUANDO
        var status = service.buscarStatus(1L);

        // ENTAO
        assertTrue(status.isEmpty());
    }
}

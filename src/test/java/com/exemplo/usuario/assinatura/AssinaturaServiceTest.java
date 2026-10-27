package com.exemplo.usuario.assinatura;

import com.exemplo.usuario.assinatura.internal.Assinatura;
import com.exemplo.usuario.assinatura.internal.AssinaturaRepository;
import com.exemplo.usuario.assinatura.internal.PlanoAssinatura;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssinaturaServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private AssinaturaRepository repository;

    private AssinaturaService service;

    @BeforeEach
    void setUp() {
        service = new AssinaturaService(repository);
    }

    @Test
    void deveCriarAssinaturaBasicaZeradaParaUsuario() {
        // DADO
        var captor = ArgumentCaptor.forClass(Assinatura.class);

        // QUANDO
        service.criarParaUsuario(USUARIO_ID);

        // ENTAO
        verify(repository).save(captor.capture());
        var salva = captor.getValue();
        assertEquals(USUARIO_ID, salva.getUsuarioId());
        assertEquals(PlanoAssinatura.BASICO, salva.getProgresso().getPlano());
        assertEquals(0, salva.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(0, salva.getCarteira().getCreditosCursos());
        assertEquals(0, salva.getCarteira().getMoedas());
    }

    @Test
    void deveRetornarResumoDaAssinatura() {
        // DADO
        when(repository.findByUsuarioId(USUARIO_ID))
                .thenReturn(Optional.of(new Assinatura(USUARIO_ID)));

        // QUANDO
        var resumo = service.resumo(USUARIO_ID);

        // ENTAO
        assertEquals(new AssinaturaResponseDTO("BASICO", 0, 0, 0), resumo);
    }

    @Test
    void deveRetornarResumoNuloQuandoNaoHaAssinatura() {
        // DADO
        when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO
        var resumo = service.resumo(USUARIO_ID);

        // ENTAO
        assertEquals(new AssinaturaResponseDTO(null, null, null, null), resumo);
    }

    @Test
    void deveConsumirCreditoQuandoHaSaldo() {
        // DADO
        var assinatura = new Assinatura(USUARIO_ID);
        assinatura.getCarteira().adicionarCreditos(2);
        when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(assinatura));

        // QUANDO
        service.consumirCredito(USUARIO_ID);

        // ENTAO
        assertEquals(1, assinatura.getCarteira().getCreditosCursos());
    }

    @Test
    void naoDeveConsumirCreditoSemSaldo() {
        // DADO
        when(repository.findByUsuarioId(USUARIO_ID))
                .thenReturn(Optional.of(new Assinatura(USUARIO_ID)));

        // QUANDO
        var erro = assertThrows(IllegalStateException.class,
                () -> service.consumirCredito(USUARIO_ID));

        // ENTAO
        assertEquals("Usuario sem creditos disponiveis para cursos bonus.", erro.getMessage());
    }

    @Test
    void naoDeveConsumirCreditoSemAssinatura() {
        // DADO
        when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO
        var erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.consumirCredito(USUARIO_ID));

        // ENTAO
        assertEquals("Assinatura nao encontrada", erro.getMessage());
    }

    @Test
    void deveRegistrarConclusaoComSucesso() {
        // DADO
        var assinatura = new Assinatura(USUARIO_ID);
        when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(assinatura));

        // QUANDO
        service.registrarConclusaoComSucesso(USUARIO_ID);

        // ENTAO
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
    }

    @Test
    void naoDeveRegistrarConclusaoSemAssinatura() {
        // DADO
        when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO
        var erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.registrarConclusaoComSucesso(USUARIO_ID));

        // ENTAO
        assertEquals("Assinatura nao encontrada", erro.getMessage());
    }
}

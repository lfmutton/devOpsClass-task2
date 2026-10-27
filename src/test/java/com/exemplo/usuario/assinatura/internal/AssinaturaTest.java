package com.exemplo.usuario.assinatura.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssinaturaTest {

    private static final Long USUARIO_ID = 10L;

    @Test
    void novaAssinaturaNasceBasicaEZerada() {
        // DADO / QUANDO
        var assinatura = new Assinatura(USUARIO_ID);

        // ENTAO
        assertEquals(USUARIO_ID, assinatura.getUsuarioId());
        assertEquals(PlanoAssinatura.BASICO, assinatura.getProgresso().getPlano());
        assertEquals(0, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(0, assinatura.getCarteira().getCreditosCursos());
        assertEquals(0, assinatura.getCarteira().getMoedas());
    }

    @Test
    void cadaConclusaoComSucessoEntregaTresCreditos() {
        // DADO
        var assinatura = new Assinatura(USUARIO_ID);

        // QUANDO
        assinatura.registrarConclusaoComSucesso();

        // ENTAO
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
    }

    @Test
    void devePromoverParaPremiumNaDecimaSegundaConclusao() {
        // DADO
        var assinatura = new Assinatura(USUARIO_ID);
        for (int i = 0; i < 11; i++) {
            assinatura.registrarConclusaoComSucesso();
        }
        assertEquals(PlanoAssinatura.BASICO, assinatura.getProgresso().getPlano());

        // QUANDO
        assinatura.registrarConclusaoComSucesso();

        // ENTAO
        assertEquals(PlanoAssinatura.PREMIUM, assinatura.getProgresso().getPlano());
        assertEquals(36, assinatura.getCarteira().getCreditosCursos());
    }

    @Test
    void deveConsumirCreditoQuandoHaSaldo() {
        // DADO
        var carteira = new CarteiraAssinatura();
        carteira.adicionarCreditos(2);

        // QUANDO
        carteira.consumirCredito();

        // ENTAO
        assertEquals(1, carteira.getCreditosCursos());
    }

    @Test
    void naoDeveConsumirCreditoSemSaldo() {
        // DADO
        var carteira = new CarteiraAssinatura();

        // QUANDO
        var erro = assertThrows(IllegalStateException.class, carteira::consumirCredito);

        // ENTAO
        assertEquals("Usuario sem creditos disponiveis para cursos bonus.", erro.getMessage());
        assertEquals(0, carteira.getCreditosCursos());
    }

    @Test
    void deveGerarResumoComOsDadosDaAssinatura() {
        // DADO
        var assinatura = new Assinatura(USUARIO_ID);
        assinatura.registrarConclusaoComSucesso();

        // QUANDO
        var resumo = assinatura.paraResumo();

        // ENTAO
        assertEquals("BASICO", resumo.plano());
        assertEquals(3, resumo.creditosCursos());
        assertEquals(1, resumo.cursosConcluidosComSucesso());
        assertEquals(0, resumo.moedas());
    }
}

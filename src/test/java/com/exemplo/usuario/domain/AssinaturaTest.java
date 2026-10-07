package com.exemplo.usuario.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssinaturaTest {

    private final Usuario usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");

    @Test
    void novoUsuarioNasceComAssinaturaBasicaZerada() {
        // DADO / QUANDO
        var assinatura = usuario.getAssinatura();

        // ENTAO
        assertSame(usuario, assinatura.getUsuario());
        assertEquals(PlanoAssinatura.BASICO, assinatura.getProgresso().getPlano());
        assertEquals(0, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(0, assinatura.getCarteira().getCreditosCursos());
        assertEquals(0, assinatura.getCarteira().getMoedas());
    }

    @Test
    void cadaConclusaoComSucessoEntregaTresCreditos() {
        // DADO
        var assinatura = new Assinatura(usuario);

        // QUANDO
        assinatura.registrarConclusaoComSucesso();

        // ENTAO
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
    }

    @Test
    void devePromoverParaPremiumNaDecimaSegundaConclusao() {
        // DADO
        var assinatura = new Assinatura(usuario);
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

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, carteira::consumirCredito);
        assertEquals(0, carteira.getCreditosCursos());
    }
}

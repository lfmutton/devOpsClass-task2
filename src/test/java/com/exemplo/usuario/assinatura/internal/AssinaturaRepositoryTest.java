package com.exemplo.usuario.assinatura.internal;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao com banco H2 em memoria.
// Garante que a assinatura continua mapeada na tabela "assinaturas"
// com as partes embutidas (@Embeddable) e a coluna usuario_id.
@DataJpaTest
@ActiveProfiles("h2")
class AssinaturaRepositoryTest {

    @Autowired
    private AssinaturaRepository repository;

    @Autowired
    private EntityManager entityManager;

    // Forca a ida ao banco e a releitura das entidades.
    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveSalvarERelerAssinaturaPeloUsuarioId() {
        // DADO
        var assinatura = new Assinatura(42L);
        assinatura.registrarConclusaoComSucesso();
        repository.save(assinatura);
        sincronizarComBanco();

        // QUANDO
        var relida = repository.findByUsuarioId(42L).orElseThrow();

        // ENTAO
        assertEquals(42L, relida.getUsuarioId());
        assertEquals(PlanoAssinatura.BASICO, relida.getProgresso().getPlano());
        assertEquals(1, relida.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(3, relida.getCarteira().getCreditosCursos());
        assertEquals(0, relida.getCarteira().getMoedas());
    }

    @Test
    void naoDeveEncontrarAssinaturaDeOutroUsuario() {
        // DADO
        repository.save(new Assinatura(42L));
        sincronizarComBanco();

        // QUANDO
        var resultado = repository.findByUsuarioId(99L);

        // ENTAO
        assertTrue(resultado.isEmpty());
    }
}

package com.exemplo.usuario.mensalidade.internal;

import com.exemplo.usuario.mensalidade.StatusMensalidade;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao com banco H2 em memoria.
// Garante que a mensalidade continua mapeada na tabela "mensalidades" com a coluna "usuario_id".
@DataJpaTest
@ActiveProfiles("h2")
class MensalidadeRepositoryTest {

    @Autowired
    private MensalidadeRepository repository;

    @Autowired
    private EntityManager entityManager;

    // Forca a ida ao banco e a releitura das entidades.
    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveSalvarAlterarERelerMensalidade() {
        // DADO
        repository.save(new Mensalidade(42L, StatusMensalidade.PENDENTE));
        sincronizarComBanco();

        // QUANDO
        var lida = repository.findByUsuarioId(42L).orElseThrow();
        lida.alterarStatus(StatusMensalidade.PAGA);
        repository.save(lida);
        sincronizarComBanco();

        // ENTAO
        var relida = repository.findByUsuarioId(42L).orElseThrow();
        assertEquals(42L, relida.getUsuarioId());
        assertTrue(relida.isPaga());
        assertEquals(1, repository.count());
    }

    @Test
    void deveRetornarVazioQuandoUsuarioNaoTemMensalidade() {
        // DADO / QUANDO / ENTAO
        assertTrue(repository.findByUsuarioId(999L).isEmpty());
    }
}

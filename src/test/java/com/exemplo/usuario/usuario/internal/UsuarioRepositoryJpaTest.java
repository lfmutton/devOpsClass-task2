package com.exemplo.usuario.usuario.internal;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao com banco H2 em memoria.
// Garante que as partes embutidas (@Embeddable) continuam mapeadas na tabela "usuarios".
@DataJpaTest
@ActiveProfiles("h2")
class UsuarioRepositoryJpaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private Usuario salvarUsuario() {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        usuario.validarAcessoPlataforma(true);
        return usuarioRepository.save(usuario);
    }

    // Forca a ida ao banco e a releitura das entidades.
    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void devePersistirERelerUsuarioComPerfilEAcesso() {
        // DADO
        Long id = salvarUsuario().getId();
        sincronizarComBanco();

        // QUANDO
        var usuario = usuarioRepository.findById(id).orElseThrow();

        // ENTAO
        assertEquals("Fulano", usuario.getPerfil().getNome());
        assertEquals("fulano@teste.com", usuario.getPerfil().getEmail());
        assertEquals("hash", usuario.getPerfil().getSenha());
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
    }

    @Test
    void deveEncontrarUsuarioPeloEmail() {
        // DADO
        salvarUsuario();
        sincronizarComBanco();

        // QUANDO / ENTAO
        assertTrue(usuarioRepository.existsByPerfilEmailValor("fulano@teste.com"));
        assertFalse(usuarioRepository.existsByPerfilEmailValor("outro@teste.com"));
    }
}

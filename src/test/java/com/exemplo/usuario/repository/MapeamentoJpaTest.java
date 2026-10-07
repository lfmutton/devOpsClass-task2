package com.exemplo.usuario.repository;

import com.exemplo.usuario.domain.Curso;
import com.exemplo.usuario.domain.Matricula;
import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.PlanoAssinatura;
import com.exemplo.usuario.domain.StatusMatricula;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao com banco H2 em memoria.
// Garante que as partes embutidas (@Embeddable) continuam mapeadas nas mesmas tabelas/colunas.
@DataJpaTest
@ActiveProfiles("h2")
class MapeamentoJpaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AssinaturaRepository assinaturaRepository;

    @Autowired
    private MensalidadeRepository mensalidadeRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private EntityManager entityManager;

    private Usuario salvarUsuario() {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        usuario.validarAcessoPlataforma();
        return usuarioRepository.save(usuario);
    }

    // Forca a ida ao banco e a releitura das entidades.
    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void devePersistirUsuarioComAssinaturaEMensalidadeEmCascata() {
        // DADO
        Long id = salvarUsuario().getId();
        sincronizarComBanco();

        // QUANDO
        var usuario = usuarioRepository.findById(id).orElseThrow();

        // ENTAO
        assertEquals("Fulano", usuario.getPerfil().getNome());
        assertEquals("fulano@teste.com", usuario.getPerfil().getEmail());
        assertEquals(PlanoAssinatura.BASICO, usuario.getAssinatura().getProgresso().getPlano());
        assertEquals(0, usuario.getAssinatura().getCarteira().getCreditosCursos());
        assertEquals(StatusMensalidade.PAGA, usuario.getAcesso().getMensalidade().getStatus());
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
        assertTrue(assinaturaRepository.findByUsuarioId(id).isPresent());
        assertTrue(mensalidadeRepository.findByUsuarioId(id).isPresent());
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

    @Test
    void devePersistirSituacaoECreditosDaMatriculaConcluida() {
        // DADO
        var usuario = salvarUsuario();
        var curso = cursoRepository.save(new Curso("Java", null));
        var matricula = new Matricula(usuario, curso, false);
        matricula.getSituacao().concluir(9.0);
        usuario.getAssinatura().registrarConclusaoComSucesso();
        Long matriculaId = matriculaRepository.save(matricula).getId();
        sincronizarComBanco();

        // QUANDO
        var lida = matriculaRepository.findById(matriculaId).orElseThrow();
        var assinatura = assinaturaRepository.findByUsuarioId(usuario.getId()).orElseThrow();

        // ENTAO
        assertEquals(StatusMatricula.CONCLUIDO, lida.getSituacao().getStatus());
        assertEquals(9.0, lida.getSituacao().getNotaFinal());
        assertEquals(null, lida.getCurso().getDescricao());
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
        assertEquals(1, matriculaRepository.findByUsuarioId(usuario.getId()).size());
    }
}

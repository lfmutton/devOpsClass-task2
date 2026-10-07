package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Assinatura;
import com.exemplo.usuario.domain.Curso;
import com.exemplo.usuario.domain.Matricula;
import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.repository.AssinaturaRepository;
import com.exemplo.usuario.repository.CursoRepository;
import com.exemplo.usuario.repository.MatriculaRepository;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AssinaturaRepository assinaturaRepository;

    private MatriculaService service;

    @BeforeEach
    void setUp() {
        service = new MatriculaService(matriculaRepository, usuarioRepository, cursoRepository, assinaturaRepository);
    }

    private Usuario usuarioComMensalidade(StatusMensalidade status) {
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, status));
        return usuario;
    }

    @Test
    void deveBloquearMatriculaQuandoUsuarioSemMensalidade() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO / ENTAO
        // O bloqueio acontece antes de buscar curso/assinatura, entao esses
        // repositories nem chegam a ser chamados.
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 1L, false));
        assertFalse(usuario.getAcesso().temAcessoAoCurso());
        assertTrue(usuario.getAcesso().isPlataformaCongelada());
        verify(cursoRepository, never()).findById(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveBloquearMatriculaQuandoMensalidadePendente() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PENDENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 1L, false));
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void devePermitirMatriculaQuandoMensalidadePaga() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var curso = new Curso("Java", "Curso de Java");
        var assinatura = new Assinatura(usuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(assinaturaRepository.findByUsuarioId(1L)).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.matricular(1L, 1L, false);

        // ENTAO
        assertNotNull(resultado);
        assertEquals("EM_ANDAMENTO", resultado.situacao().status());
        assertFalse(resultado.situacao().bonus());
        assertTrue(usuario.getAcesso().temAcessoAoCurso());
        assertFalse(usuario.getAcesso().isPlataformaCongelada());
        verify(matriculaRepository).save(any());
    }

    @Test
    void deveConcederCreditoAoConcluirComNotaAltaEMensalidadePaga() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), false);
        var assinatura = new Assinatura(usuario);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(assinaturaRepository.findByUsuarioId(any())).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        var resultado = service.concluir(10L, 9.0);

        // ENTAO
        assertEquals("CONCLUIDO", resultado.situacao().status());
        assertEquals(9.0, resultado.situacao().notaFinal());
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
    }

    @Test
    void naoDeveConcederCreditoQuandoNotaAbaixoDeSete() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), false);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        service.concluir(10L, 5.0);

        // ENTAO
        verify(assinaturaRepository, never()).findByUsuarioId(any());
    }

    @Test
    void naoDeveConcluirAMesmaMatriculaDuasVezes() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), false);
        var assinatura = new Assinatura(usuario);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(assinaturaRepository.findByUsuarioId(any())).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any())).thenReturn(matricula);
        service.concluir(10L, 9.0);

        // QUANDO / ENTAO
        var ex = assertThrows(IllegalStateException.class, () -> service.concluir(10L, 9.0));
        assertEquals("Matricula ja concluida", ex.getMessage());
        // Os creditos foram concedidos uma unica vez.
        assertEquals(3, assinatura.getCarteira().getCreditosCursos());
        assertEquals(1, assinatura.getProgresso().getCursosConcluidosComSucesso());
    }

    @Test
    void deveListarMatriculasDoUsuarioConvertidasEmDTO() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), true);
        when(matriculaRepository.findByUsuarioId(1L)).thenReturn(List.of(matricula));

        // QUANDO
        var resultado = service.listarPorUsuario(1L);

        // ENTAO
        assertEquals(1, resultado.size());
        var dto = resultado.get(0);
        assertEquals("Fulano", dto.alunoCurso().usuarioNome());
        assertEquals("Java", dto.alunoCurso().cursoTitulo());
        assertEquals("EM_ANDAMENTO", dto.situacao().status());
        assertTrue(dto.situacao().bonus());
    }

    @Test
    void deveLancarExcecaoAoMatricularUsuarioInexistente() {
        // DADO
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.matricular(1L, 1L, false));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoMatricularEmCursoInexistente() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(2L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.matricular(1L, 2L, false));
        assertEquals("Curso nao encontrado", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoMatricularUsuarioSemAssinatura() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(new Curso("Java", "Curso de Java")));
        when(assinaturaRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.matricular(1L, 2L, false));
        assertEquals("Assinatura nao encontrada", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveConsumirCreditoAoMatricularComBonus() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var assinatura = new Assinatura(usuario);
        assinatura.getCarteira().adicionarCreditos(2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(new Curso("Java", "Curso de Java")));
        when(assinaturaRepository.findByUsuarioId(1L)).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.matricular(1L, 1L, true);

        // ENTAO
        assertEquals(1, assinatura.getCarteira().getCreditosCursos());
        assertTrue(resultado.situacao().bonus());
    }

    @Test
    void naoDeveMatricularComBonusQuandoSemCreditos() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var assinatura = new Assinatura(usuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(new Curso("Java", "Curso de Java")));
        when(assinaturaRepository.findByUsuarioId(1L)).thenReturn(Optional.of(assinatura));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 1L, true));
        assertEquals(0, assinatura.getCarteira().getCreditosCursos());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConcluirMatriculaInexistente() {
        // DADO
        when(matriculaRepository.findById(10L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.concluir(10L, 9.0));
        assertEquals("Matricula nao encontrada", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConcluirComNotaAltaSemAssinatura() {
        // DADO
        var usuario = usuarioComMensalidade(StatusMensalidade.PAGA);
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), false);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(assinaturaRepository.findByUsuarioId(any())).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.concluir(10L, 9.0));
        assertEquals("Assinatura nao encontrada", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveBloquearConclusaoQuandoUsuarioSemMensalidadePaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), false);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.concluir(10L, 9.0));
        verify(matriculaRepository, never()).save(any());
        verify(assinaturaRepository, never()).findByUsuarioId(any());
    }
}

package com.exemplo.usuario.matricula.internal;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.curso.CursoResponseDTO;
import com.exemplo.usuario.curso.CursoService;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.UsuarioApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testa os casos de uso de matricula com os outros modulos mockados.
// O mapper e real (usa os mesmos mocks de UsuarioApi e CursoService).
@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private UsuarioApi usuarioApi;

    @Mock
    private CursoService cursoService;

    @Mock
    private AssinaturaService assinaturaService;

    private MatriculaService service;

    @BeforeEach
    void setUp() {
        var mapper = new MatriculaResponseMapper(usuarioApi, cursoService);
        service = new MatriculaService(matriculaRepository, usuarioApi, cursoService, assinaturaService, mapper);
    }

    // Simula o usuario 1 ("Fulano") e o curso 2 ("Java") usados na montagem do DTO.
    private void usuarioECursoExistentes() {
        when(usuarioApi.buscarNome(1L)).thenReturn("Fulano");
        when(cursoService.buscarPorId(2L)).thenReturn(new CursoResponseDTO(2L, "Java", "Curso de Java"));
    }

    @Test
    void deveBloquearMatriculaQuandoUsuarioSemAcesso() {
        // DADO
        doThrow(new IllegalStateException("Acesso bloqueado")).when(usuarioApi).validarAcessoPlataforma(1L);

        // QUANDO / ENTAO
        // O bloqueio acontece antes de buscar o curso e de mexer na assinatura.
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 2L, true));
        verify(cursoService, never()).buscarPorId(any());
        verify(assinaturaService, never()).consumirCredito(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoMatricularUsuarioInexistente() {
        // DADO
        doThrow(new RecursoNaoEncontradoException("Usuario nao encontrado"))
                .when(usuarioApi).validarAcessoPlataforma(1L);

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.matricular(1L, 2L, false));
        assertEquals("Usuario nao encontrado", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoMatricularEmCursoInexistente() {
        // DADO
        when(cursoService.buscarPorId(2L)).thenThrow(new RecursoNaoEncontradoException("Curso nao encontrado"));

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.matricular(1L, 2L, true));
        assertEquals("Curso nao encontrado", ex.getMessage());
        verify(assinaturaService, never()).consumirCredito(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveMatricularSemBonusSemConsumirCredito() {
        // DADO
        usuarioECursoExistentes();
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.matricular(1L, 2L, false);

        // ENTAO
        assertEquals(1L, resultado.alunoCurso().usuarioId());
        assertEquals("Fulano", resultado.alunoCurso().usuarioNome());
        assertEquals(2L, resultado.alunoCurso().cursoId());
        assertEquals("Java", resultado.alunoCurso().cursoTitulo());
        assertEquals("EM_ANDAMENTO", resultado.situacao().status());
        assertFalse(resultado.situacao().bonus());
        verify(assinaturaService, never()).consumirCredito(any());
        verify(matriculaRepository).save(any());
    }

    @Test
    void deveConsumirCreditoAoMatricularComBonus() {
        // DADO
        usuarioECursoExistentes();
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.matricular(1L, 2L, true);

        // ENTAO
        assertTrue(resultado.situacao().bonus());
        verify(assinaturaService).consumirCredito(1L);
    }

    @Test
    void naoDeveMatricularComBonusQuandoSemCreditos() {
        // DADO
        doThrow(new IllegalStateException("Usuario sem creditos disponiveis para cursos bonus."))
                .when(assinaturaService).consumirCredito(1L);

        // QUANDO / ENTAO
        var ex = assertThrows(IllegalStateException.class, () -> service.matricular(1L, 2L, true));
        assertEquals("Usuario sem creditos disponiveis para cursos bonus.", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveRegistrarConclusaoComSucessoAoConcluirComNotaAlta() {
        // DADO
        usuarioECursoExistentes();
        var matricula = new Matricula(1L, 2L, false);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        var resultado = service.concluir(10L, 9.0);

        // ENTAO
        assertEquals("CONCLUIDO", resultado.situacao().status());
        assertEquals(9.0, resultado.situacao().notaFinal());
        verify(assinaturaService).registrarConclusaoComSucesso(1L);
    }

    @Test
    void naoDeveRegistrarConclusaoQuandoNotaAbaixoDeSete() {
        // DADO
        usuarioECursoExistentes();
        var matricula = new Matricula(1L, 2L, false);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        var resultado = service.concluir(10L, 5.0);

        // ENTAO
        assertEquals("CONCLUIDO", resultado.situacao().status());
        verify(assinaturaService, never()).registrarConclusaoComSucesso(any());
    }

    @Test
    void naoDeveConcluirAMesmaMatriculaDuasVezes() {
        // DADO
        usuarioECursoExistentes();
        var matricula = new Matricula(1L, 2L, false);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any())).thenReturn(matricula);
        service.concluir(10L, 9.0);

        // QUANDO / ENTAO
        var ex = assertThrows(IllegalStateException.class, () -> service.concluir(10L, 9.0));
        assertEquals("Matricula ja concluida", ex.getMessage());
        // Os creditos foram registrados uma unica vez.
        verify(assinaturaService, times(1)).registrarConclusaoComSucesso(1L);
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
    void deveBloquearConclusaoQuandoUsuarioSemAcesso() {
        // DADO
        var matricula = new Matricula(1L, 2L, false);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        doThrow(new IllegalStateException("Acesso bloqueado")).when(usuarioApi).validarAcessoPlataforma(1L);

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.concluir(10L, 9.0));
        assertEquals(StatusMatricula.EM_ANDAMENTO, matricula.getSituacao().getStatus());
        verify(assinaturaService, never()).registrarConclusaoComSucesso(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConcluirComNotaAltaSemAssinatura() {
        // DADO
        var matricula = new Matricula(1L, 2L, false);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        doThrow(new RecursoNaoEncontradoException("Assinatura nao encontrada"))
                .when(assinaturaService).registrarConclusaoComSucesso(1L);

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.concluir(10L, 9.0));
        assertEquals("Assinatura nao encontrada", ex.getMessage());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveListarMatriculasDoUsuarioConvertidasEmDTO() {
        // DADO
        usuarioECursoExistentes();
        var matricula = new Matricula(1L, 2L, true);
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
}

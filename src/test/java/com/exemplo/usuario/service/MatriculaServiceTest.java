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

    @Test
    void deveBloquearMatriculaQuandoUsuarioSemMensalidade() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO / ENTAO
        // O bloqueio acontece antes de buscar curso/assinatura, entao esses
        // repositories nem chegam a ser chamados.
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 1L, false));
        assertFalse(usuario.temAcessoAoCurso());
        assertTrue(usuario.isPlataformaCongelada());
        verify(cursoRepository, never()).findById(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void deveBloquearMatriculaQuandoMensalidadePendente() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PENDENTE));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.matricular(1L, 1L, false));
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void devePermitirMatriculaQuandoMensalidadePaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        var curso = new Curso("Java", "Curso de Java");
        var assinatura = new Assinatura(usuario);
        var matriculaSalva = new Matricula(usuario, curso, false);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(assinaturaRepository.findByUsuarioId(1L)).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any())).thenReturn(matriculaSalva);

        // QUANDO
        var resultado = service.matricular(1L, 1L, false);

        // ENTAO
        assertNotNull(resultado);
        assertTrue(usuario.temAcessoAoCurso());
        assertFalse(usuario.isPlataformaCongelada());
        verify(matriculaRepository).save(any());
    }

    @Test
    void deveConcederCreditoAoConcluirComNotaAltaEMensalidadePaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);
        var assinatura = new Assinatura(usuario);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(assinaturaRepository.findByUsuarioId(any())).thenReturn(Optional.of(assinatura));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        var resultado = service.concluir(10L, 9.0);

        // ENTAO
        assertNotNull(resultado);
        assertEquals(3, assinatura.getCreditosCursos());
        assertEquals(1, assinatura.getCursosConcluidosComSucesso());
    }

    @Test
    void naoDeveConcederCreditoQuandoNotaAbaixoDeSete() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        usuario.vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any())).thenReturn(matricula);

        // QUANDO
        service.concluir(10L, 5.0);

        // ENTAO
        verify(assinaturaRepository, never()).findByUsuarioId(any());
    }

    @Test
    void deveBloquearConclusaoQuandoUsuarioSemMensalidadePaga() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "senha123");
        var curso = new Curso("Java", "Curso de Java");
        var matricula = new Matricula(usuario, curso, false);

        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));

        // QUANDO / ENTAO
        assertThrows(IllegalStateException.class, () -> service.concluir(10L, 9.0));
        verify(matriculaRepository, never()).save(any());
        verify(assinaturaRepository, never()).findByUsuarioId(any());
    }
}

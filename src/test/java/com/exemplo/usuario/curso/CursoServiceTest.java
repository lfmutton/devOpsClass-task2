package com.exemplo.usuario.curso;

import com.exemplo.usuario.curso.internal.Curso;
import com.exemplo.usuario.curso.internal.CursoRepository;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository repository;

    private CursoService service;

    @BeforeEach
    void setUp() {
        service = new CursoService(repository);
    }

    @Test
    void deveListarTodosOsCursosConvertidosEmDTO() {
        // DADO
        when(repository.findAll()).thenReturn(List.of(
                new Curso("Java", "Curso de Java"),
                new Curso("Spring", null)
        ));

        // QUANDO
        var resultado = service.listarTodos();

        // ENTAO
        assertEquals(2, resultado.size());
        assertEquals("Java", resultado.get(0).titulo());
        assertEquals("Curso de Java", resultado.get(0).descricao());
        assertEquals("Spring", resultado.get(1).titulo());
        assertNull(resultado.get(1).descricao());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaCursos() {
        // DADO
        when(repository.findAll()).thenReturn(List.of());

        // QUANDO / ENTAO
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void deveCriarCursoComDadosNormalizados() {
        // DADO
        var dto = new CursoRequestDTO();
        dto.setTitulo("  Java  ");
        dto.setDescricao("  Curso de Java  ");
        when(repository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        // QUANDO
        var resultado = service.criar(dto);

        // ENTAO
        var captor = ArgumentCaptor.forClass(Curso.class);
        verify(repository).save(captor.capture());
        assertEquals("Java", captor.getValue().getTitulo());
        assertEquals("Java", resultado.titulo());
        assertEquals("Curso de Java", resultado.descricao());
    }

    @Test
    void naoDeveCriarCursoSemTitulo() {
        // DADO
        var dto = new CursoRequestDTO();
        dto.setTitulo("   ");

        // QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> service.criar(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void deveBuscarCursoPorId() {
        // DADO
        when(repository.findById(1L)).thenReturn(Optional.of(new Curso("Java", "Curso de Java")));

        // QUANDO
        var resultado = service.buscarPorId(1L);

        // ENTAO
        assertEquals("Java", resultado.titulo());
        assertEquals("Curso de Java", resultado.descricao());
    }

    @Test
    void deveLancarExcecaoQuandoCursoNaoExiste() {
        // DADO
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO
        var erro = assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));

        // ENTAO
        assertEquals("Curso nao encontrado", erro.getMessage());
    }
}

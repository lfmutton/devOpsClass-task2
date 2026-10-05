package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Curso;
import com.exemplo.usuario.dto.CursoRequestDTO;
import com.exemplo.usuario.repository.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        assertEquals("Java", resultado.get(0).getTitulo());
        assertEquals("Curso de Java", resultado.get(0).getDescricao());
        assertEquals("Spring", resultado.get(1).getTitulo());
        assertNull(resultado.get(1).getDescricao());
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
        assertEquals("Java", resultado.getTitulo());
        assertEquals("Curso de Java", resultado.getDescricao());
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
}

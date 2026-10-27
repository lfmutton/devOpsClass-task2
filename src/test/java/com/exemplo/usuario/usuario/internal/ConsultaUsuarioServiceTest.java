package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaResponseDTO;
import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.StatusMensalidade;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaUsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private MensalidadeService mensalidadeService;

    private ConsultaUsuarioService service;

    @BeforeEach
    void setUp() {
        // O mapper e real: so os modulos vizinhos (assinatura e mensalidade) sao mockados.
        var mapper = new UsuarioResponseMapper(assinaturaService, mensalidadeService);
        service = new ConsultaUsuarioService(repository, mapper);
    }

    private Usuario usuarioComId(Long id, String nome, String email) {
        var usuario = new Usuario(nome, email, "hash");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    void deveListarTodosOsUsuarios() {
        // DADO
        // Um usuario com mensalidade e assinatura e outro sem, para cobrir os dois lados do DTO.
        var comMensalidade = usuarioComId(1L, "Fulano", "fulano@teste.com");
        var semMensalidade = usuarioComId(2L, "Ciclano", "ciclano@teste.com");
        when(repository.findAll()).thenReturn(List.of(comMensalidade, semMensalidade));
        when(assinaturaService.resumo(1L)).thenReturn(new AssinaturaResponseDTO("BASICO", 0, 0, 0));
        when(assinaturaService.resumo(2L)).thenReturn(new AssinaturaResponseDTO(null, null, null, null));
        when(mensalidadeService.buscarStatus(1L)).thenReturn(Optional.of(StatusMensalidade.PAGA));
        when(mensalidadeService.buscarStatus(2L)).thenReturn(Optional.empty());

        // QUANDO
        var resultado = service.listarTodos();

        // ENTAO
        assertEquals(2, resultado.size());
        assertEquals("BASICO", resultado.get(0).assinatura().plano());
        assertEquals(0, resultado.get(0).assinatura().creditosCursos());
        assertEquals("PAGA", resultado.get(0).acesso().statusMensalidade());
        assertNull(resultado.get(1).assinatura().plano());
        assertNull(resultado.get(1).acesso().statusMensalidade());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        // DADO
        var usuario = usuarioComId(1L, "Fulano", "fulano@teste.com");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(assinaturaService.resumo(1L)).thenReturn(new AssinaturaResponseDTO("BASICO", 0, 0, 0));
        when(mensalidadeService.buscarStatus(1L)).thenReturn(Optional.of(StatusMensalidade.PENDENTE));

        // QUANDO
        var resultado = service.buscarPorId(1L);

        // ENTAO
        assertEquals(1L, resultado.id());
        assertEquals("Fulano", resultado.nome());
        assertEquals("fulano@teste.com", resultado.email());
        assertEquals("PENDENTE", resultado.acesso().statusMensalidade());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // DADO
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTAO
        var ex = assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
        assertEquals("Usuario nao encontrado", ex.getMessage());
    }
}

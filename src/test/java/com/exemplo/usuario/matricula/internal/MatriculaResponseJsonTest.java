package com.exemplo.usuario.matricula.internal;

import com.exemplo.usuario.curso.CursoResponseDTO;
import com.exemplo.usuario.curso.CursoService;
import com.exemplo.usuario.usuario.UsuarioApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Garante que o MatriculaResponseDTO continua gerando o mesmo JSON "plano"
// que o frontend (static/js/app.js e frontend-vue) consome.
@ExtendWith(MockitoExtension.class)
class MatriculaResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioApi usuarioApi;

    @Mock
    private CursoService cursoService;

    private List<String> chaves(JsonNode json) {
        List<String> chaves = new ArrayList<>();
        json.fieldNames().forEachRemaining(chaves::add);
        return chaves;
    }

    @Test
    void matriculaResponseMantemFormatoPlano() {
        // DADO
        when(usuarioApi.buscarNome(1L)).thenReturn("Fulano");
        when(cursoService.buscarPorId(2L)).thenReturn(new CursoResponseDTO(2L, "Java", "Curso de Java"));
        var mapper = new MatriculaResponseMapper(usuarioApi, cursoService);
        var matricula = new Matricula(1L, 2L, true);
        matricula.getSituacao().concluir(8.5);

        // QUANDO
        JsonNode json = objectMapper.valueToTree(mapper.paraDTO(matricula));

        // ENTAO
        assertEquals(List.of("id", "usuarioId", "usuarioNome", "cursoId", "cursoTitulo",
                "status", "notaFinal", "bonus"), chaves(json));
        assertEquals(1L, json.get("usuarioId").asLong());
        assertEquals("Fulano", json.get("usuarioNome").asText());
        assertEquals(2L, json.get("cursoId").asLong());
        assertEquals("Java", json.get("cursoTitulo").asText());
        assertEquals("CONCLUIDO", json.get("status").asText());
        assertEquals(8.5, json.get("notaFinal").asDouble());
        assertTrue(json.get("bonus").asBoolean());
    }
}

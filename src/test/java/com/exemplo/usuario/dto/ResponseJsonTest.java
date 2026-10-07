package com.exemplo.usuario.dto;

import com.exemplo.usuario.domain.Curso;
import com.exemplo.usuario.domain.Matricula;
import com.exemplo.usuario.domain.Mensalidade;
import com.exemplo.usuario.domain.StatusMensalidade;
import com.exemplo.usuario.domain.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Garante que os DTOs divididos continuam gerando o mesmo JSON "plano"
// que o frontend (static/js/app.js e frontend-vue) consome.
class ResponseJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private List<String> chaves(JsonNode json) {
        List<String> chaves = new ArrayList<>();
        json.fieldNames().forEachRemaining(chaves::add);
        return chaves;
    }

    @Test
    void usuarioResponseMantemFormatoPlano() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        usuario.getAcesso().vincularMensalidade(new Mensalidade(usuario, StatusMensalidade.PAGA));
        usuario.validarAcessoPlataforma();

        // QUANDO
        JsonNode json = mapper.valueToTree(UsuarioResponseDTO.de(usuario));

        // ENTAO
        assertEquals(List.of("id", "nome", "email", "plano", "creditosCursos", "cursosConcluidosComSucesso",
                "moedas", "statusMensalidade", "temAcessoAoCurso", "plataformaCongelada"), chaves(json));
        assertEquals("Fulano", json.get("nome").asText());
        assertEquals("BASICO", json.get("plano").asText());
        assertEquals("PAGA", json.get("statusMensalidade").asText());
        assertTrue(json.get("temAcessoAoCurso").asBoolean());
    }

    @Test
    void usuarioSemAssinaturaGeraCamposNulos() {
        // QUANDO
        JsonNode json = mapper.valueToTree(AssinaturaResponseDTO.de(null));

        // ENTAO
        assertEquals(List.of("plano", "creditosCursos", "cursosConcluidosComSucesso", "moedas"), chaves(json));
        assertTrue(json.get("plano").isNull());
        assertTrue(json.get("moedas").isNull());
    }

    @Test
    void matriculaResponseMantemFormatoPlano() {
        // DADO
        var usuario = new Usuario("Fulano", "fulano@teste.com", "hash");
        var matricula = new Matricula(usuario, new Curso("Java", "Curso de Java"), true);
        matricula.getSituacao().concluir(8.5);

        // QUANDO
        JsonNode json = mapper.valueToTree(MatriculaResponseDTO.de(matricula));

        // ENTAO
        assertEquals(List.of("id", "usuarioId", "usuarioNome", "cursoId", "cursoTitulo",
                "status", "notaFinal", "bonus"), chaves(json));
        assertEquals("Fulano", json.get("usuarioNome").asText());
        assertEquals("Java", json.get("cursoTitulo").asText());
        assertEquals("CONCLUIDO", json.get("status").asText());
        assertEquals(8.5, json.get("notaFinal").asDouble());
        assertTrue(json.get("bonus").asBoolean());
    }
}

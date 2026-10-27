package com.exemplo.usuario.usuario.internal;

import com.exemplo.usuario.assinatura.AssinaturaResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Garante que o UsuarioResponseDTO continua gerando o mesmo JSON "plano"
// que o frontend (static/js/app.js e frontend-vue) consome.
class UsuarioResponseJsonTest {

    private static final List<String> CHAVES_ESPERADAS = List.of("id", "nome", "email", "plano",
            "creditosCursos", "cursosConcluidosComSucesso", "moedas",
            "statusMensalidade", "temAcessoAoCurso", "plataformaCongelada");

    private final ObjectMapper mapper = new ObjectMapper();

    private List<String> chaves(JsonNode json) {
        List<String> chaves = new ArrayList<>();
        json.fieldNames().forEachRemaining(chaves::add);
        return chaves;
    }

    @Test
    void usuarioResponseMantemFormatoPlano() {
        // DADO
        var dto = new UsuarioResponseDTO(1L, "Fulano", "fulano@teste.com",
                new AssinaturaResponseDTO("BASICO", 0, 0, 0),
                new AcessoResponseDTO("PAGA", true, false));

        // QUANDO
        JsonNode json = mapper.valueToTree(dto);

        // ENTAO
        assertEquals(CHAVES_ESPERADAS, chaves(json));
        assertEquals(1L, json.get("id").asLong());
        assertEquals("Fulano", json.get("nome").asText());
        assertEquals("BASICO", json.get("plano").asText());
        assertEquals(0, json.get("moedas").asInt());
        assertEquals("PAGA", json.get("statusMensalidade").asText());
        assertTrue(json.get("temAcessoAoCurso").asBoolean());
        assertFalse(json.get("plataformaCongelada").asBoolean());
    }

    @Test
    void usuarioSemAssinaturaESemMensalidadeGeraCamposNulos() {
        // DADO
        var dto = new UsuarioResponseDTO(2L, "Ciclano", "ciclano@teste.com",
                new AssinaturaResponseDTO(null, null, null, null),
                new AcessoResponseDTO(null, false, true));

        // QUANDO
        JsonNode json = mapper.valueToTree(dto);

        // ENTAO
        // Mesmo com valores nulos, as chaves continuam presentes e na mesma ordem.
        assertEquals(CHAVES_ESPERADAS, chaves(json));
        assertTrue(json.get("plano").isNull());
        assertTrue(json.get("moedas").isNull());
        assertTrue(json.get("statusMensalidade").isNull());
        assertTrue(json.get("plataformaCongelada").asBoolean());
    }
}

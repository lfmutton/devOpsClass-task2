package com.exemplo.usuario.mensalidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusMensalidadeTest {

    @Test
    void deveIgnorarEspacosEMinusculas() {
        // DADO
        var texto = " paga ";

        // QUANDO
        var status = StatusMensalidade.de(texto);

        // ENTAO
        assertEquals(StatusMensalidade.PAGA, status);
    }

    @Test
    void deveConverterPendenteEmMinusculas() {
        // DADO / QUANDO
        var status = StatusMensalidade.de("pendente");

        // ENTAO
        assertEquals(StatusMensalidade.PENDENTE, status);
    }

    @Test
    void deveRejeitarStatusInvalido() {
        // DADO / QUANDO / ENTAO
        assertThrows(IllegalArgumentException.class, () -> StatusMensalidade.de("ATRASADA"));
    }
}

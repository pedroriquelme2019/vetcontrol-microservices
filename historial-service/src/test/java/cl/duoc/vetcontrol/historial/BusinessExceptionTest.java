package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void debeConservarMensaje() {
        BusinessException exception =
                new BusinessException(
                        "Referencia duplicada"
                );

        assertEquals(
                "Referencia duplicada",
                exception.getMessage()
        );
    }
}
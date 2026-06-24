package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void debeConservarMensaje() {
        BusinessException exception =
                new BusinessException(
                        "Tipo no permitido"
                );

        assertEquals(
                "Tipo no permitido",
                exception.getMessage()
        );
    }
}
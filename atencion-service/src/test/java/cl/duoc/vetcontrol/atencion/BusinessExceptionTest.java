package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void debeConservarMensaje() {

        BusinessException exception =
                new BusinessException(
                        "Cita duplicada"
                );

        assertEquals(
                "Cita duplicada",
                exception.getMessage()
        );
    }
}
package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void debeGuardarMensaje() {

        BusinessException ex =
                new BusinessException("error");

        assertEquals(
                "error",
                ex.getMessage()
        );
    }
}
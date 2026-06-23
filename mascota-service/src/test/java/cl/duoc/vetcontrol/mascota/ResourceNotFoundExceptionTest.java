package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void debeGuardarMensaje() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException("no existe");

        assertEquals(
                "no existe",
                ex.getMessage()
        );
    }
}
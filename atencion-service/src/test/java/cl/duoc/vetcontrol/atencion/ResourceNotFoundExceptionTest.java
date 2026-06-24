package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    void debeConservarMensaje() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Atención no encontrada: 99"
                );

        assertEquals(
                "Atención no encontrada: 99",
                exception.getMessage()
        );
    }
}
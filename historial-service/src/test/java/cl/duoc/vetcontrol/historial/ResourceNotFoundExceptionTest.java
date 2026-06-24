package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    void debeConservarMensaje() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Historial no encontrado: 99"
                );

        assertEquals(
                "Historial no encontrado: 99",
                exception.getMessage()
        );
    }
}
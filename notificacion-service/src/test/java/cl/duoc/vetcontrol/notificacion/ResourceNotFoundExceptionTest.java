package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    void debeConservarMensaje() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Notificación no encontrada: 99"
                );

        assertEquals(
                "Notificación no encontrada: 99",
                exception.getMessage()
        );
    }
}
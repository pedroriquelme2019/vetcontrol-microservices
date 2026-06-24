package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    void debeConservarMensaje() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Cita no encontrada: 99"
                );

        assertEquals(
                "Cita no encontrada: 99",
                exception.getMessage()
        );
    }
}
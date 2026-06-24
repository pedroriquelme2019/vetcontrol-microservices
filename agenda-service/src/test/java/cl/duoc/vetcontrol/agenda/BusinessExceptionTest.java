package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void debeConservarMensaje() {
        BusinessException exception =
                new BusinessException("Horario ocupado");

        assertEquals(
                "Horario ocupado",
                exception.getMessage()
        );
    }
}
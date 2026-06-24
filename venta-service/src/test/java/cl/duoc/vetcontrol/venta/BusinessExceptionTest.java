package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void debeConservarMensaje() {
        BusinessException exception =
                new BusinessException(
                        "Stock insuficiente"
                );

        assertEquals(
                "Stock insuficiente",
                exception.getMessage()
        );
    }
}
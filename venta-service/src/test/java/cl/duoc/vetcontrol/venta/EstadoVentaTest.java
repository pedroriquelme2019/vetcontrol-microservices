package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoVentaTest {

    @Test
    void debeContenerTodosLosEstados() {
        assertArrayEquals(
                new EstadoVenta[]{
                        EstadoVenta.PENDIENTE,
                        EstadoVenta.REGISTRADA,
                        EstadoVenta.ANULADA
                },
                EstadoVenta.values()
        );
    }

    @Test
    void valueOfDebeRecuperarEstado() {
        assertEquals(
                EstadoVenta.REGISTRADA,
                EstadoVenta.valueOf("REGISTRADA")
        );
    }
}
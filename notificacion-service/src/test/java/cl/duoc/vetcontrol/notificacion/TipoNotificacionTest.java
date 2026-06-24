package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TipoNotificacionTest {

    @Test
    void debeContenerTodosLosTipos() {
        assertArrayEquals(
                new TipoNotificacion[]{
                        TipoNotificacion.CITA,
                        TipoNotificacion.VENTA,
                        TipoNotificacion.ATENCION,
                        TipoNotificacion.SISTEMA,
                        TipoNotificacion.MANUAL
                },
                TipoNotificacion.values()
        );
    }

    @Test
    void valueOfDebeRecuperarTipo() {
        assertEquals(
                TipoNotificacion.VENTA,
                TipoNotificacion.valueOf(
                        "VENTA"
                )
        );
    }
}
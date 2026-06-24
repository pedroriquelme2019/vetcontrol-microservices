package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionModelTest {

    @Test
    void notificacionNuevaDebeTenerValoresPredeterminados() {
        Notificacion notificacion =
                new Notificacion();

        assertAll(
                () -> assertNull(
                        notificacion.getId()
                ),
                () -> assertNull(
                        notificacion.getTipo()
                ),
                () -> assertNull(
                        notificacion.getMensaje()
                ),
                () -> assertNotNull(
                        notificacion.getFecha()
                ),
                () -> assertFalse(
                        notificacion.isLeida()
                ),
                () -> assertNull(
                        notificacion.getOrigenEvento()
                ),
                () -> assertNull(
                        notificacion.getReferenciaExternaId()
                ),
                () -> assertNull(
                        notificacion.getClaveEvento()
                )
        );
    }

    @Test
    void gettersYSettersDebenFuncionar() {
        Notificacion notificacion =
                new Notificacion();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        25,
                        12,
                        0
                );

        notificacion.setId(1L);
        notificacion.setTipo(
                TipoNotificacion.VENTA
        );
        notificacion.setMensaje(
                "Venta registrada"
        );
        notificacion.setFecha(fecha);
        notificacion.setLeida(true);
        notificacion.setOrigenEvento(
                "venta-creada"
        );
        notificacion.setReferenciaExternaId(50L);
        notificacion.setClaveEvento(
                "venta-creada:50"
        );

        assertAll(
                () -> assertEquals(
                        1L,
                        notificacion.getId()
                ),
                () -> assertEquals(
                        TipoNotificacion.VENTA,
                        notificacion.getTipo()
                ),
                () -> assertEquals(
                        "Venta registrada",
                        notificacion.getMensaje()
                ),
                () -> assertEquals(
                        fecha,
                        notificacion.getFecha()
                ),
                () -> assertTrue(
                        notificacion.isLeida()
                ),
                () -> assertEquals(
                        "venta-creada",
                        notificacion.getOrigenEvento()
                ),
                () -> assertEquals(
                        50L,
                        notificacion.getReferenciaExternaId()
                ),
                () -> assertEquals(
                        "venta-creada:50",
                        notificacion.getClaveEvento()
                )
        );
    }

    @Test
    void prePersistDebeAsignarFechaCuandoEsNula() {
        Notificacion notificacion =
                new Notificacion();

        notificacion.setFecha(null);

        notificacion.prePersist();

        assertNotNull(
                notificacion.getFecha()
        );
    }

    @Test
    void prePersistNoDebeModificarFechaExistente() {
        Notificacion notificacion =
                new Notificacion();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        20,
                        10,
                        0
                );

        notificacion.setFecha(fecha);

        notificacion.prePersist();

        assertEquals(
                fecha,
                notificacion.getFecha()
        );
    }
}
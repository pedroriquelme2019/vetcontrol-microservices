package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HistorialClinicoTest {

    @Test
    void gettersYSettersDebenFuncionar() {
        HistorialClinico historial =
                new HistorialClinico();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        20,
                        10,
                        30
                );

        historial.setId(1L);
        historial.setMascotaId(10L);
        historial.setFecha(fecha);
        historial.setTipo("VACUNA");
        historial.setDetalle(
                "Vacuna antirrábica"
        );
        historial.setReferenciaExternaId(50L);

        assertAll(
                () -> assertEquals(
                        1L,
                        historial.getId()
                ),
                () -> assertEquals(
                        10L,
                        historial.getMascotaId()
                ),
                () -> assertEquals(
                        fecha,
                        historial.getFecha()
                ),
                () -> assertEquals(
                        "VACUNA",
                        historial.getTipo()
                ),
                () -> assertEquals(
                        "Vacuna antirrábica",
                        historial.getDetalle()
                ),
                () -> assertEquals(
                        50L,
                        historial.getReferenciaExternaId()
                )
        );
    }

    @Test
    void historialNuevoDebeTenerCamposNulos() {
        HistorialClinico historial =
                new HistorialClinico();

        assertAll(
                () -> assertNull(historial.getId()),
                () -> assertNull(
                        historial.getMascotaId()
                ),
                () -> assertNull(historial.getFecha()),
                () -> assertNull(historial.getTipo()),
                () -> assertNull(historial.getDetalle()),
                () -> assertNull(
                        historial.getReferenciaExternaId()
                )
        );
    }
}
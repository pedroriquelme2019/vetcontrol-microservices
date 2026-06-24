package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.model.Atencion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AtencionModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        Atencion atencion = new Atencion();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        20,
                        15,
                        30
                );

        atencion.setId(1L);
        atencion.setCitaId(5L);
        atencion.setMascotaId(10L);
        atencion.setVeterinarioId(20L);
        atencion.setFechaAtencion(fecha);
        atencion.setDiagnostico("Dermatitis");
        atencion.setTratamiento("Antihistamínico");
        atencion.setObservaciones("Control");
        atencion.setActivo(false);

        assertAll(
                () -> assertEquals(1L, atencion.getId()),
                () -> assertEquals(5L, atencion.getCitaId()),
                () -> assertEquals(10L, atencion.getMascotaId()),
                () -> assertEquals(
                        20L,
                        atencion.getVeterinarioId()
                ),
                () -> assertEquals(
                        fecha,
                        atencion.getFechaAtencion()
                ),
                () -> assertEquals(
                        "Dermatitis",
                        atencion.getDiagnostico()
                ),
                () -> assertEquals(
                        "Antihistamínico",
                        atencion.getTratamiento()
                ),
                () -> assertEquals(
                        "Control",
                        atencion.getObservaciones()
                ),
                () -> assertFalse(atencion.isActivo())
        );
    }

    @Test
    void atencionNuevaDebeEstarActivaPorDefecto() {

        Atencion atencion = new Atencion();

        assertAll(
                () -> assertNull(atencion.getId()),
                () -> assertNull(atencion.getCitaId()),
                () -> assertNull(atencion.getMascotaId()),
                () -> assertNull(atencion.getVeterinarioId()),
                () -> assertNull(atencion.getFechaAtencion()),
                () -> assertNull(atencion.getDiagnostico()),
                () -> assertNull(atencion.getTratamiento()),
                () -> assertNull(atencion.getObservaciones()),
                () -> assertTrue(atencion.isActivo())
        );
    }
}
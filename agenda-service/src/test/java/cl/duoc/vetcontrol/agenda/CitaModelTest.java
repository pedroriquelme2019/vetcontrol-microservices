package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {
        Cita cita = new Cita();

        LocalDate fecha = LocalDate.of(2026, 7, 10);
        LocalTime hora = LocalTime.of(10, 30);

        cita.setId(1L);
        cita.setMascotaId(10L);
        cita.setVeterinarioId(20L);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo("Control");
        cita.setEstado(EstadoCita.CONFIRMADA);

        assertAll(
                () -> assertEquals(1L, cita.getId()),
                () -> assertEquals(10L, cita.getMascotaId()),
                () -> assertEquals(20L, cita.getVeterinarioId()),
                () -> assertEquals(fecha, cita.getFecha()),
                () -> assertEquals(hora, cita.getHora()),
                () -> assertEquals("Control", cita.getMotivo()),
                () -> assertEquals(
                        EstadoCita.CONFIRMADA,
                        cita.getEstado()
                )
        );
    }

    @Test
    void citaNuevaDebeQuedarProgramada() {
        Cita cita = new Cita();

        assertAll(
                () -> assertNull(cita.getId()),
                () -> assertNull(cita.getMascotaId()),
                () -> assertNull(cita.getVeterinarioId()),
                () -> assertNull(cita.getFecha()),
                () -> assertNull(cita.getHora()),
                () -> assertNull(cita.getMotivo()),
                () -> assertEquals(
                        EstadoCita.PROGRAMADA,
                        cita.getEstado()
                )
        );
    }
}
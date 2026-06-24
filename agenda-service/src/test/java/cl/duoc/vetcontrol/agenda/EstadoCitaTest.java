package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoCitaTest {

    @Test
    void debeContenerTodosLosEstados() {
        EstadoCita[] estados = EstadoCita.values();

        assertArrayEquals(
                new EstadoCita[]{
                        EstadoCita.PROGRAMADA,
                        EstadoCita.CONFIRMADA,
                        EstadoCita.COMPLETADA,
                        EstadoCita.CANCELADA
                },
                estados
        );
    }

    @Test
    void valueOfDebeRecuperarEstado() {
        assertEquals(
                EstadoCita.COMPLETADA,
                EstadoCita.valueOf("COMPLETADA")
        );
    }
}
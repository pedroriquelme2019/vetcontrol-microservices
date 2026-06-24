package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import cl.duoc.vetcontrol.agenda.repository.CitaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class CitaRepositoryTest {

    @Autowired
    private CitaRepository repository;

    @Test
    void saveDebeGuardarCitaCompleta() {
        Cita guardada = repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        EstadoCita.PROGRAMADA
                )
        );

        assertAll(
                () -> assertNotNull(guardada.getId()),
                () -> assertEquals(10L, guardada.getMascotaId()),
                () -> assertEquals(20L, guardada.getVeterinarioId()),
                () -> assertEquals("Control", guardada.getMotivo()),
                () -> assertEquals(EstadoCita.PROGRAMADA, guardada.getEstado())
        );
    }

    @Test
    void findByEstadoNotDebeExcluirCanceladas() {
        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        EstadoCita.PROGRAMADA
                )
        );

        repository.saveAndFlush(
                crearCita(
                        11L,
                        21L,
                        LocalDate.of(2026, 7, 11),
                        LocalTime.of(11, 0),
                        EstadoCita.CANCELADA
                )
        );

        List<Cita> resultado =
                repository.findByEstadoNot(EstadoCita.CANCELADA);

        assertEquals(1, resultado.size());
        assertEquals(EstadoCita.PROGRAMADA, resultado.get(0).getEstado());
    }

    @Test
    void findByIdAndEstadoNotDebeIgnorarCancelada() {
        Cita cancelada = repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        EstadoCita.CANCELADA
                )
        );

        Optional<Cita> resultado =
                repository.findByIdAndEstadoNot(
                        cancelada.getId(),
                        EstadoCita.CANCELADA
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByMascotaDebeExcluirCanceladas() {
        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        EstadoCita.PROGRAMADA
                )
        );

        repository.saveAndFlush(
                crearCita(
                        10L,
                        21L,
                        LocalDate.of(2026, 7, 11),
                        LocalTime.of(11, 0),
                        EstadoCita.CANCELADA
                )
        );

        List<Cita> resultado =
                repository.findByMascotaIdAndEstadoNot(
                        10L,
                        EstadoCita.CANCELADA
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void findByVeterinarioDebeExcluirCanceladas() {
        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        EstadoCita.PROGRAMADA
                )
        );

        repository.saveAndFlush(
                crearCita(
                        11L,
                        20L,
                        LocalDate.of(2026, 7, 11),
                        LocalTime.of(11, 0),
                        EstadoCita.CANCELADA
                )
        );

        List<Cita> resultado =
                repository.findByVeterinarioIdAndEstadoNot(
                        20L,
                        EstadoCita.CANCELADA
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void findByFechaDebeExcluirCanceladas() {
        LocalDate fecha = LocalDate.of(2026, 7, 10);

        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        fecha,
                        LocalTime.of(10, 0),
                        EstadoCita.PROGRAMADA
                )
        );

        repository.saveAndFlush(
                crearCita(
                        11L,
                        21L,
                        fecha,
                        LocalTime.of(11, 0),
                        EstadoCita.CANCELADA
                )
        );

        List<Cita> resultado =
                repository.findByFechaAndEstadoNot(
                        fecha,
                        EstadoCita.CANCELADA
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void existsByHorarioDebeIgnorarCitasCanceladas() {
        LocalDate fecha = LocalDate.of(2026, 7, 10);
        LocalTime hora = LocalTime.of(10, 0);

        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        fecha,
                        hora,
                        EstadoCita.CANCELADA
                )
        );

        boolean ocupado =
                repository
                        .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                                20L,
                                fecha,
                                hora,
                                EstadoCita.CANCELADA
                        );

        assertFalse(ocupado);
    }

    @Test
    void existsByHorarioDebeDetectarCitaActiva() {
        LocalDate fecha = LocalDate.of(2026, 7, 10);
        LocalTime hora = LocalTime.of(10, 0);

        repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        fecha,
                        hora,
                        EstadoCita.PROGRAMADA
                )
        );

        assertTrue(
                repository
                        .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                                20L,
                                fecha,
                                hora,
                                EstadoCita.CANCELADA
                        )
        );
    }

    @Test
    void existsByHorarioEIdNotDebeExcluirLaMismaCita() {
        LocalDate fecha = LocalDate.of(2026, 7, 10);
        LocalTime hora = LocalTime.of(10, 0);

        Cita cita = repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        fecha,
                        hora,
                        EstadoCita.PROGRAMADA
                )
        );

        boolean ocupado =
                repository
                        .existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
                                20L,
                                fecha,
                                hora,
                                cita.getId(),
                                EstadoCita.CANCELADA
                        );

        assertFalse(ocupado);
    }

    @Test
    void existsByHorarioEIdNotDebeDetectarOtraCita() {
        LocalDate fecha = LocalDate.of(2026, 7, 10);
        LocalTime hora = LocalTime.of(10, 0);

        Cita primera = repository.saveAndFlush(
                crearCita(
                        10L,
                        20L,
                        fecha,
                        hora,
                        EstadoCita.PROGRAMADA
                )
        );

        repository.saveAndFlush(
                crearCita(
                        11L,
                        20L,
                        fecha,
                        hora,
                        EstadoCita.PROGRAMADA
                )
        );

        assertTrue(
                repository
                        .existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
                                20L,
                                fecha,
                                hora,
                                primera.getId(),
                                EstadoCita.CANCELADA
                        )
        );
    }

    private Cita crearCita(
            Long mascotaId,
            Long veterinarioId,
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    ) {
        Cita cita = new Cita();

        cita.setMascotaId(mascotaId);
        cita.setVeterinarioId(veterinarioId);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo("Control");
        cita.setEstado(estado);

        return cita;
    }
}
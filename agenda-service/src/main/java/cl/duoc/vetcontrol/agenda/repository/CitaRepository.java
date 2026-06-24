package cl.duoc.vetcontrol.agenda.repository;

import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository
        extends JpaRepository<Cita, Long> {

    List<Cita> findByEstadoNot(
            EstadoCita estado
    );

    Optional<Cita> findByIdAndEstadoNot(
            Long id,
            EstadoCita estado
    );

    List<Cita> findByVeterinarioIdAndEstadoNot(
            Long veterinarioId,
            EstadoCita estado
    );

    List<Cita> findByMascotaIdAndEstadoNot(
            Long mascotaId,
            EstadoCita estado
    );

    List<Cita> findByFechaAndEstadoNot(
            LocalDate fecha,
            EstadoCita estado
    );

    boolean existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
            Long veterinarioId,
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    boolean existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
            Long veterinarioId,
            LocalDate fecha,
            LocalTime hora,
            Long id,
            EstadoCita estado
    );
}
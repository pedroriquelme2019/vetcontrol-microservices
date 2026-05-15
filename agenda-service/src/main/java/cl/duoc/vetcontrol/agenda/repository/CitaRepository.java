package cl.duoc.vetcontrol.agenda.repository;
import cl.duoc.vetcontrol.agenda.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.*; import java.util.List;
public interface CitaRepository extends JpaRepository<Cita,Long>{ boolean existsByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora); List<Cita> findByVeterinarioId(Long veterinarioId); List<Cita> findByMascotaId(Long mascotaId); List<Cita> findByFecha(LocalDate fecha); }

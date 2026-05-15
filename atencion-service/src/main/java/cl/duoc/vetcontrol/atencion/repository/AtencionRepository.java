package cl.duoc.vetcontrol.atencion.repository;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AtencionRepository extends JpaRepository<Atencion,Long>{ List<Atencion> findByMascotaId(Long mascotaId); List<Atencion> findByVeterinarioId(Long veterinarioId); }

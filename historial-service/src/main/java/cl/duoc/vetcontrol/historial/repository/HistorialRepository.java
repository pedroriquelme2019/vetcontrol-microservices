package cl.duoc.vetcontrol.historial.repository;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface HistorialRepository extends JpaRepository<HistorialClinico,Long>{ List<HistorialClinico> findByMascotaIdOrderByFechaDesc(Long mascotaId); }

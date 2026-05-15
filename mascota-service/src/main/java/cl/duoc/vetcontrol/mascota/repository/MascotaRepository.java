package cl.duoc.vetcontrol.mascota.repository;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByClienteId(Long clienteId);
    List<Mascota> findByNombreContainingIgnoreCase(String nombre);
}

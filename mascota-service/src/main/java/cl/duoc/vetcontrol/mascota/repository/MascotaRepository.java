package cl.duoc.vetcontrol.mascota.repository;

import cl.duoc.vetcontrol.mascota.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByActivoTrue();

    Optional<Mascota> findByIdAndActivoTrue(Long id);

    List<Mascota> findByClienteIdAndActivoTrue(Long clienteId);

    List<Mascota> findByNombreContainingIgnoreCaseAndActivoTrue(
            String nombre
    );
}
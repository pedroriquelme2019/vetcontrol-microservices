package cl.duoc.vetcontrol.atencion.repository;

import cl.duoc.vetcontrol.atencion.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtencionRepository
        extends JpaRepository<Atencion, Long> {

    List<Atencion> findByActivoTrue();

    Optional<Atencion> findByIdAndActivoTrue(Long id);

    List<Atencion> findByMascotaIdAndActivoTrue(
            Long mascotaId
    );

    List<Atencion> findByVeterinarioIdAndActivoTrue(
            Long veterinarioId
    );

    boolean existsByCitaIdAndActivoTrue(
            Long citaId
    );

    boolean existsByCitaIdAndIdNotAndActivoTrue(
            Long citaId,
            Long id
    );
}
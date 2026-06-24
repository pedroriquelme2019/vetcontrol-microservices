package cl.duoc.vetcontrol.historial.repository;

import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistorialRepository
        extends JpaRepository<HistorialClinico, Long> {

    List<HistorialClinico> findAllByOrderByFechaDesc();

    List<HistorialClinico> findByMascotaIdOrderByFechaDesc(
            Long mascotaId
    );

    Optional<HistorialClinico> findByTipoAndReferenciaExternaId(
            String tipo,
            Long referenciaExternaId
    );
}
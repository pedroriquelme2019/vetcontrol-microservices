package cl.duoc.vetcontrol.inventario.repository;

import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository
        extends JpaRepository<InventarioItem, Long> {

    List<InventarioItem> findByActivoTrue();

    Optional<InventarioItem> findByProductoId(
            Long productoId
    );

    Optional<InventarioItem> findByProductoIdAndActivoTrue(
            Long productoId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventarioItem i
            WHERE i.productoId = :productoId
              AND i.activo = true
            """)
    Optional<InventarioItem> findByProductoIdForUpdate(
            @Param("productoId") Long productoId
    );

    @Query("""
            SELECT i
            FROM InventarioItem i
            WHERE i.activo = true
              AND i.stockActual <= i.stockMinimo
            """)
    List<InventarioItem> findBajoStock();
}
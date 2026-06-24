package cl.duoc.vetcontrol.producto.repository;

import cl.duoc.vetcontrol.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    Optional<Producto> findByIdAndActivoTrue(Long id);

    List<Producto> findByCategoriaIgnoreCaseAndActivoTrue(
            String categoria
    );

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(
            String nombre
    );
}
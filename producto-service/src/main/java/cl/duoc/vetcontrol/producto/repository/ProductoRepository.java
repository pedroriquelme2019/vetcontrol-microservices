package cl.duoc.vetcontrol.producto.repository;
import cl.duoc.vetcontrol.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductoRepository extends JpaRepository<Producto,Long>{ List<Producto> findByCategoriaIgnoreCase(String categoria); List<Producto> findByNombreContainingIgnoreCase(String nombre); }

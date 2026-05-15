package cl.duoc.vetcontrol.inventario.repository;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InventarioRepository extends JpaRepository<InventarioItem,Long>{ Optional<InventarioItem> findByProductoId(Long productoId); }

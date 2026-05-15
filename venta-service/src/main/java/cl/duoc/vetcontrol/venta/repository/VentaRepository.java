package cl.duoc.vetcontrol.venta.repository;
import cl.duoc.vetcontrol.venta.model.Venta; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface VentaRepository extends JpaRepository<Venta,Long>{ List<Venta> findByClienteId(Long clienteId); }

package cl.duoc.vetcontrol.cliente.repository;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByRut(String rut);
    boolean existsByCorreo(String correo);
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}

package cl.duoc.vetcontrol.cliente.repository;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByRutAndIdNot(String rut, Long id);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    List<Cliente> findByActivoTrue();

    List<Cliente> findByNombreContainingIgnoreCaseAndActivoTrue(
            String nombre
    );

    Optional<Cliente> findByIdAndActivoTrue(Long id);
}
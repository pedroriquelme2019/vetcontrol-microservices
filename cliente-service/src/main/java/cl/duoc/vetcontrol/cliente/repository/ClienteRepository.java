package cl.duoc.vetcontrol.cliente.repository;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRut(String rut);
    boolean existsByCorreo(String correo);

    // CORREGIDO: solo retorna clientes activos (para que el soft-delete tenga efecto)
    List<Cliente> findByActivoTrue();

    // CORREGIDO: busca solo entre clientes activos
    List<Cliente> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Útil para buscar por id solo si está activo
    Optional<Cliente> findByIdAndActivoTrue(Long id);
}

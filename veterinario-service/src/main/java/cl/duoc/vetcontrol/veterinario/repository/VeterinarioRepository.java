package cl.duoc.vetcontrol.veterinario.repository;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> { boolean existsByRut(String rut); boolean existsByCorreo(String correo); List<Veterinario> findByEspecialidadContainingIgnoreCase(String especialidad); }

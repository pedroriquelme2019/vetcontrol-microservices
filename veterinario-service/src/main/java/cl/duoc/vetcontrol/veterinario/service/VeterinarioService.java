package cl.duoc.vetcontrol.veterinario.service;
import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import cl.duoc.vetcontrol.veterinario.exception.BusinessException;
import cl.duoc.vetcontrol.veterinario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class VeterinarioService {
 private final VeterinarioRepository repository; public VeterinarioService(VeterinarioRepository repository){this.repository=repository;}
 public List<Veterinario> findAll(){return repository.findAll();}
 public Veterinario findById(Long id){return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado: "+id));}
 public Veterinario create(VeterinarioRequest r){ if(repository.existsByRut(r.rut())) throw new BusinessException("RUT duplicado"); if(repository.existsByCorreo(r.correo())) throw new BusinessException("Correo duplicado"); return repository.save(map(new Veterinario(), r));}
 public Veterinario update(Long id,VeterinarioRequest r){return repository.save(map(findById(id),r));}
 public void delete(Long id){Veterinario v=findById(id); v.setActivo(false); repository.save(v);} public List<Veterinario> byEspecialidad(String e){return repository.findByEspecialidadContainingIgnoreCase(e);} private Veterinario map(Veterinario v,VeterinarioRequest r){v.setRut(r.rut());v.setNombre(r.nombre());v.setEspecialidad(r.especialidad());v.setCorreo(r.correo());return v;}
}

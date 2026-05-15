package cl.duoc.vetcontrol.cliente.service;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.exception.BusinessException;
import cl.duoc.vetcontrol.cliente.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteService {
    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository repository;
    public ClienteService(ClienteRepository repository) { this.repository = repository; }
    public List<Cliente> findAll() { return repository.findAll(); }
    public Cliente findById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id)); }
    public Cliente create(ClienteRequest request) {
        if (repository.existsByRut(request.rut())) throw new BusinessException("Ya existe un cliente con ese RUT");
        if (repository.existsByCorreo(request.correo())) throw new BusinessException("Ya existe un cliente con ese correo");
        Cliente c = map(new Cliente(), request);
        log.info("Creando cliente {}", c.getRut());
        return repository.save(c);
    }
    public Cliente update(Long id, ClienteRequest request) { Cliente c = findById(id); return repository.save(map(c, request)); }
    public void delete(Long id) { Cliente c = findById(id); c.setActivo(false); repository.save(c); }
    public List<Cliente> search(String nombre) { return repository.findByNombreContainingIgnoreCase(nombre); }
    private Cliente map(Cliente c, ClienteRequest r) { c.setRut(r.rut()); c.setNombre(r.nombre()); c.setTelefono(r.telefono()); c.setCorreo(r.correo()); c.setDireccion(r.direccion()); return c; }
}

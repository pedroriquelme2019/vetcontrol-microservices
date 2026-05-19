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

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    // CORREGIDO: solo retorna clientes activos
    public List<Cliente> findAll() {
        return repository.findByActivoTrue();
    }

    // CORREGIDO: solo encuentra clientes activos por id
    public Cliente findById(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    public Cliente create(ClienteRequest request) {
        if (repository.existsByRut(request.rut())) {
            throw new BusinessException("Ya existe un cliente con ese RUT");
        }
        if (repository.existsByCorreo(request.correo())) {
            throw new BusinessException("Ya existe un cliente con ese correo");
        }
        Cliente c = map(new Cliente(), request);
        log.info("Creando cliente {}", c.getRut());
        return repository.save(c);
    }

    public Cliente update(Long id, ClienteRequest request) {
        Cliente c = findById(id);
        return repository.save(map(c, request));
    }

    // Soft-delete: marca activo=false. Con findByActivoTrue en el repo ya no aparece.
    public void delete(Long id) {
        Cliente c = findById(id);
        c.setActivo(false);
        repository.save(c);
        log.info("Cliente {} marcado como inactivo (soft-delete)", id);
    }

    // CORREGIDO: busca solo entre activos
    public List<Cliente> search(String nombre) {
        return repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    private Cliente map(Cliente c, ClienteRequest r) {
        c.setRut(r.rut());
        c.setNombre(r.nombre());
        c.setTelefono(r.telefono());
        c.setCorreo(r.correo());
        c.setDireccion(r.direccion());
        return c;
    }
}

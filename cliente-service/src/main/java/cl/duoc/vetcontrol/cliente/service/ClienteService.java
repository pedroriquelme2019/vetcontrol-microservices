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

    private static final Logger log =
            LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> findAll() {
        return repository.findByActivoTrue();
    }

    public Cliente findById(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente no encontrado: " + id
                        )
                );
    }

    public Cliente create(ClienteRequest request) {

        if (repository.existsByRut(request.rut())) {
            throw new BusinessException(
                    "Ya existe un cliente con ese RUT"
            );
        }

        if (repository.existsByCorreo(request.correo())) {
            throw new BusinessException(
                    "Ya existe un cliente con ese correo"
            );
        }

        Cliente cliente = map(
                new Cliente(),
                request
        );

        log.info(
                "Creando cliente {}",
                cliente.getRut()
        );

        return repository.save(cliente);
    }

    public Cliente update(
            Long id,
            ClienteRequest request
    ) {
        Cliente cliente = findById(id);

        if (repository.existsByRutAndIdNot(
                request.rut(),
                id
        )) {
            throw new BusinessException(
                    "Ya existe otro cliente con ese RUT"
            );
        }

        if (repository.existsByCorreoAndIdNot(
                request.correo(),
                id
        )) {
            throw new BusinessException(
                    "Ya existe otro cliente con ese correo"
            );
        }

        map(cliente, request);

        log.info(
                "Actualizando cliente {}",
                id
        );

        return repository.save(cliente);
    }

    public void delete(Long id) {

        Cliente cliente = findById(id);

        cliente.setActivo(false);

        repository.save(cliente);

        log.info(
                "Cliente {} marcado como inactivo",
                id
        );
    }

    public List<Cliente> search(String nombre) {
        return repository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        nombre
                );
    }

    private Cliente map(
            Cliente cliente,
            ClienteRequest request
    ) {
        cliente.setRut(request.rut());
        cliente.setNombre(request.nombre());
        cliente.setTelefono(request.telefono());
        cliente.setCorreo(request.correo());
        cliente.setDireccion(request.direccion());

        return cliente;
    }
}
package cl.duoc.vetcontrol.mascota.service;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.repository.MascotaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MascotaService {

    private static final Logger log =
            LoggerFactory.getLogger(MascotaService.class);

    private final MascotaRepository repository;
    private final ClienteClient clienteClient;

    public MascotaService(
            MascotaRepository repository,
            ClienteClient clienteClient
    ) {
        this.repository = repository;
        this.clienteClient = clienteClient;
    }

    public List<Mascota> findAll() {
        return repository.findByActivoTrue();
    }

    public Mascota findById(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mascota no encontrada: " + id
                        )
                );
    }

    public List<Mascota> findByCliente(Long clienteId) {
        return repository.findByClienteIdAndActivoTrue(
                clienteId
        );
    }

    public List<Mascota> search(String nombre) {
        return repository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        nombre
                );
    }

    public Mascota create(MascotaRequest request) {

        validarCliente(request.clienteId());

        Mascota mascota = map(
                new Mascota(),
                request
        );

        log.info(
                "Creando mascota {} para el cliente {}",
                mascota.getNombre(),
                mascota.getClienteId()
        );

        return repository.save(mascota);
    }

    public Mascota update(
            Long id,
            MascotaRequest request
    ) {
        // Primero verificamos que la mascota exista.
        Mascota mascota = findById(id);

        // Después verificamos que el nuevo dueño sea válido.
        validarCliente(request.clienteId());

        map(mascota, request);

        log.info(
                "Actualizando mascota {}",
                id
        );

        return repository.save(mascota);
    }

    public void delete(Long id) {

        Mascota mascota = findById(id);

        mascota.setActivo(false);

        repository.save(mascota);

        log.info(
                "Mascota {} marcada como inactiva",
                id
        );
    }

    private void validarCliente(Long clienteId) {

        try {
            Map<String, Object> cliente =
                    clienteClient.findById(clienteId);

            if (cliente == null || cliente.isEmpty()) {
                throw new BusinessException(
                        "El cliente dueño no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "El cliente dueño no existe o no está disponible"
            );
        }
    }

    private Mascota map(
            Mascota mascota,
            MascotaRequest request
    ) {
        mascota.setClienteId(request.clienteId());
        mascota.setNombre(request.nombre());
        mascota.setEspecie(request.especie());
        mascota.setRaza(request.raza());
        mascota.setEdad(request.edad());
        mascota.setSexo(request.sexo());
        mascota.setPeso(request.peso());
        mascota.setMicrochip(request.microchip());

        return mascota;
    }
}
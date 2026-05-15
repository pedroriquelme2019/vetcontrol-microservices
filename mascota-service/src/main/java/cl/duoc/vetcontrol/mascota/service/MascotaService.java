package cl.duoc.vetcontrol.mascota.service;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.repository.MascotaRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MascotaService {
    private final MascotaRepository repository; private final ClienteClient clienteClient;
    public MascotaService(MascotaRepository repository, ClienteClient clienteClient) { this.repository = repository; this.clienteClient = clienteClient; }
    public List<Mascota> findAll() { return repository.findAll(); }
    public Mascota findById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada: " + id)); }
    public List<Mascota> findByCliente(Long clienteId) { return repository.findByClienteId(clienteId); }
    public Mascota create(MascotaRequest r) { validarCliente(r.clienteId()); return repository.save(map(new Mascota(), r)); }
    public Mascota update(Long id, MascotaRequest r) { validarCliente(r.clienteId()); return repository.save(map(findById(id), r)); }
    public void delete(Long id) { Mascota m = findById(id); m.setActivo(false); repository.save(m); }
    private void validarCliente(Long id) { try { clienteClient.findById(id); } catch (FeignException ex) { throw new BusinessException("El cliente dueño no existe o no está disponible"); } }
    private Mascota map(Mascota m, MascotaRequest r) { m.setClienteId(r.clienteId()); m.setNombre(r.nombre()); m.setEspecie(r.especie()); m.setRaza(r.raza()); m.setEdad(r.edad()); m.setSexo(r.sexo()); m.setPeso(r.peso()); m.setMicrochip(r.microchip()); return m; }
}

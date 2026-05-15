package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ClienteServiceTest {
    @Test
    void createDebeGuardarCliente() {
        ClienteRepository repo = Mockito.mock(ClienteRepository.class);
        ClienteService service = new ClienteService(repo);
        ClienteRequest request = new ClienteRequest("99999999-9", "Cliente Test", "+56999999999", "test@example.com", "Dirección Test");
        when(repo.existsByRut(request.rut())).thenReturn(false);
        when(repo.existsByCorreo(request.correo())).thenReturn(false);
        when(repo.save(Mockito.any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cliente result = service.create(request);
        assertEquals("Cliente Test", result.getNombre());
    }
}

package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.exception.BusinessException;
import cl.duoc.vetcontrol.cliente.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    private ClienteRepository repository;
    private ClienteService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ClienteRepository.class);
        service = new ClienteService(repository);
    }

    @Test
    void mainDebeEjecutarse() {
        ClienteServiceApplication.main(new String[]{});
    }

    @Test
    void createDebeGuardarCliente() {

        ClienteRequest request = new ClienteRequest(
                "99999999-9",
                "Cliente Test",
                "+56999999999",
                "test@example.com",
                "Dirección Test"
        );

        when(repository.existsByRut(request.rut())).thenReturn(false);
        when(repository.existsByCorreo(request.correo())).thenReturn(false);

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cliente result = service.create(request);

        assertNotNull(result);
        assertEquals("Cliente Test", result.getNombre());
        assertEquals("99999999-9", result.getRut());

        verify(repository).save(any(Cliente.class));
    }

    @Test
    void createDebeLanzarExcepcionCuandoRutExiste() {

        ClienteRequest request = new ClienteRequest(
                "11111111-1",
                "Pedro",
                "+56911111111",
                "pedro@test.cl",
                "Santiago"
        );

        when(repository.existsByRut(request.rut())).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void createDebeLanzarExcepcionCuandoCorreoExiste() {

        ClienteRequest request = new ClienteRequest(
                "22222222-2",
                "Carlos",
                "+56922222222",
                "correo@test.cl",
                "Valparaíso"
        );

        when(repository.existsByRut(request.rut())).thenReturn(false);
        when(repository.existsByCorreo(request.correo())).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void findAllDebeRetornarClientesActivos() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Pedro");

        when(repository.findByActivoTrue())
                .thenReturn(List.of(cliente));

        List<Cliente> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Pedro", resultado.get(0).getNombre());
    }

    @Test
    void findByIdDebeRetornarCliente() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("María");

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        Cliente resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("María", resultado.getNombre());
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(1L)
        );
    }

    @Test
    void updateDebeModificarCliente() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Nombre Antiguo");

        ClienteRequest request = new ClienteRequest(
                "12345678-9",
                "Nombre Nuevo",
                "+56912345678",
                "nuevo@test.cl",
                "Nueva Dirección"
        );

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cliente resultado = service.update(1L, request);

        assertEquals("Nombre Nuevo", resultado.getNombre());
        assertEquals("12345678-9", resultado.getRut());

        verify(repository).save(any(Cliente.class));
    }

    @Test
    void deleteDebeDesactivarCliente() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setActivo(true);

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(1L);

        assertFalse(cliente.isActivo());

        verify(repository).save(cliente);
    }

    @Test
    void searchDebeRetornarClientesPorNombre() {

        Cliente cliente = new Cliente();
        cliente.setNombre("Pedro");

        when(repository.findByNombreContainingIgnoreCaseAndActivoTrue("Pedro"))
                .thenReturn(List.of(cliente));

        List<Cliente> resultado = service.search("Pedro");

        assertEquals(1, resultado.size());
        assertEquals("Pedro", resultado.get(0).getNombre());
    }
}
package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.exception.BusinessException;
import cl.duoc.vetcontrol.cliente.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    private ClienteRepository repository;
    private ClienteService service;

    @BeforeEach
    void setUp() {
        repository = mock(ClienteRepository.class);
        service = new ClienteService(repository);
    }

    @Test
    void findAllDebeRetornarSoloClientesActivos() {

        Cliente cliente = crearCliente();

        when(repository.findByActivoTrue())
                .thenReturn(List.of(cliente));

        List<Cliente> resultado =
                service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(
                "Joaquín González",
                resultado.get(0).getNombre()
        );

        verify(repository).findByActivoTrue();
    }

    @Test
    void findByIdDebeRetornarClienteActivo() {

        Cliente cliente = crearCliente();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        Cliente resultado =
                service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertTrue(resultado.isActivo());
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findById(99L)
                );

        assertEquals(
                "Cliente no encontrado: 99",
                exception.getMessage()
        );
    }

    @Test
    void createDebeGuardarClienteCompleto() {

        ClienteRequest request = crearRequest();

        when(repository.existsByRut(request.rut()))
                .thenReturn(false);

        when(repository.existsByCorreo(request.correo()))
                .thenReturn(false);

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Cliente resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(
                        request.rut(),
                        resultado.getRut()
                ),
                () -> assertEquals(
                        request.nombre(),
                        resultado.getNombre()
                ),
                () -> assertEquals(
                        request.telefono(),
                        resultado.getTelefono()
                ),
                () -> assertEquals(
                        request.correo(),
                        resultado.getCorreo()
                ),
                () -> assertEquals(
                        request.direccion(),
                        resultado.getDireccion()
                ),
                () -> assertTrue(resultado.isActivo()),
                () -> assertNotNull(resultado.getCreatedAt())
        );

        verify(repository).save(any(Cliente.class));
    }

    @Test
    void createDebeRechazarRutDuplicado() {

        ClienteRequest request = crearRequest();

        when(repository.existsByRut(request.rut()))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "Ya existe un cliente con ese RUT",
                exception.getMessage()
        );

        verify(repository, never())
                .existsByCorreo(anyString());

        verify(repository, never())
                .save(any());
    }

    @Test
    void createDebeRechazarCorreoDuplicado() {

        ClienteRequest request = crearRequest();

        when(repository.existsByRut(request.rut()))
                .thenReturn(false);

        when(repository.existsByCorreo(request.correo()))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "Ya existe un cliente con ese correo",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void updateDebeActualizarTodosLosCampos() {

        Cliente cliente = crearCliente();

        LocalDateTime fechaOriginal =
                cliente.getCreatedAt();

        ClienteRequest request =
                new ClienteRequest(
                        "22222222-2",
                        "Joaquín Actualizado",
                        "+56988888888",
                        "nuevo@correo.cl",
                        "Dirección actualizada"
                );

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        when(repository.existsByRutAndIdNot(
                request.rut(),
                1L
        )).thenReturn(false);

        when(repository.existsByCorreoAndIdNot(
                request.correo(),
                1L
        )).thenReturn(false);

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Cliente resultado =
                service.update(1L, request);

        assertAll(
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        "22222222-2",
                        resultado.getRut()
                ),
                () -> assertEquals(
                        "Joaquín Actualizado",
                        resultado.getNombre()
                ),
                () -> assertEquals(
                        "+56988888888",
                        resultado.getTelefono()
                ),
                () -> assertEquals(
                        "nuevo@correo.cl",
                        resultado.getCorreo()
                ),
                () -> assertEquals(
                        "Dirección actualizada",
                        resultado.getDireccion()
                ),
                () -> assertEquals(
                        fechaOriginal,
                        resultado.getCreatedAt()
                ),
                () -> assertTrue(resultado.isActivo())
        );
    }

    @Test
    void updateDebeRechazarRutDeOtroCliente() {

        Cliente cliente = crearCliente();
        ClienteRequest request = crearRequest();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        when(repository.existsByRutAndIdNot(
                request.rut(),
                1L
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(1L, request)
                );

        assertEquals(
                "Ya existe otro cliente con ese RUT",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void updateDebeRechazarCorreoDeOtroCliente() {

        Cliente cliente = crearCliente();
        ClienteRequest request = crearRequest();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        when(repository.existsByRutAndIdNot(
                request.rut(),
                1L
        )).thenReturn(false);

        when(repository.existsByCorreoAndIdNot(
                request.correo(),
                1L
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(1L, request)
                );

        assertEquals(
                "Ya existe otro cliente con ese correo",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void updateDebeLanzarExcepcionCuandoClienteNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(
                        99L,
                        crearRequest()
                )
        );

        verify(repository, never())
                .existsByRutAndIdNot(
                        anyString(),
                        anyLong()
                );

        verify(repository, never())
                .save(any());
    }

    @Test
    void deleteDebeRealizarEliminacionLogica() {

        Cliente cliente = crearCliente();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(cliente));

        service.delete(1L);

        ArgumentCaptor<Cliente> captor =
                ArgumentCaptor.forClass(Cliente.class);

        verify(repository).save(captor.capture());

        assertFalse(
                captor.getValue().isActivo()
        );
    }

    @Test
    void deleteDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void searchDebeBuscarSoloClientesActivos() {

        Cliente cliente = crearCliente();

        when(repository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        "joa"
                ))
                .thenReturn(List.of(cliente));

        List<Cliente> resultado =
                service.search("joa");

        assertEquals(1, resultado.size());
        assertEquals(
                "Joaquín González",
                resultado.get(0).getNombre()
        );
    }

    private ClienteRequest crearRequest() {
        return new ClienteRequest(
                "11111111-1",
                "Joaquín González",
                "+56999999999",
                "joaquin@correo.cl",
                "Recoleta, Santiago"
        );
    }

    private Cliente crearCliente() {

        Cliente cliente = new Cliente();

        cliente.setId(1L);
        cliente.setRut("11111111-1");
        cliente.setNombre("Joaquín González");
        cliente.setTelefono("+56999999999");
        cliente.setCorreo("joaquin@correo.cl");
        cliente.setDireccion("Recoleta, Santiago");
        cliente.setActivo(true);
        cliente.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        6,
                        23,
                        12,
                        0
                )
        );

        return cliente;
    }
}
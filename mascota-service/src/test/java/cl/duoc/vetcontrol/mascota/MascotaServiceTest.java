package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.repository.MascotaRepository;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MascotaServiceTest {

    private MascotaRepository repository;
    private ClienteClient clienteClient;
    private MascotaService service;

    @BeforeEach
    void setUp() {
        repository = mock(MascotaRepository.class);
        clienteClient = mock(ClienteClient.class);
        service = new MascotaService(repository, clienteClient);
    }

    @Test
    void findAllDebeRetornarSoloMascotasActivas() {

        Mascota mascota = crearMascota();

        when(repository.findByActivoTrue())
                .thenReturn(List.of(mascota));

        List<Mascota> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Firulais", resultado.get(0).getNombre());

        verify(repository).findByActivoTrue();
    }

    @Test
    void findByIdDebeRetornarMascotaActiva() {

        Mascota mascota = crearMascota();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mascota));

        Mascota resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertTrue(resultado.isActivo());
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(99L)
        );

        assertEquals(
                "Mascota no encontrada: 99",
                exception.getMessage()
        );
    }

    @Test
    void findByClienteDebeRetornarMascotasActivas() {

        Mascota mascota = crearMascota();

        when(repository.findByClienteIdAndActivoTrue(1L))
                .thenReturn(List.of(mascota));

        List<Mascota> resultado = service.findByCliente(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getClienteId());

        verify(repository)
                .findByClienteIdAndActivoTrue(1L);
    }

    @Test
    void searchDebeBuscarMascotasActivasPorNombre() {

        Mascota mascota = crearMascota();

        when(repository
                .findByNombreContainingIgnoreCaseAndActivoTrue("fir"))
                .thenReturn(List.of(mascota));

        List<Mascota> resultado = service.search("fir");

        assertEquals(1, resultado.size());
        assertEquals("Firulais", resultado.get(0).getNombre());
    }

    @Test
    void createDebeValidarClienteYGuardarTodosLosCampos() {

        MascotaRequest request = crearRequest();

        when(clienteClient.findById(1L))
                .thenReturn(Map.of(
                        "id", 1L,
                        "nombre", "Cliente válido",
                        "activo", true
                ));

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mascota resultado = service.create(request);

        assertAll(
                () -> assertEquals(1L, resultado.getClienteId()),
                () -> assertEquals("Firulais", resultado.getNombre()),
                () -> assertEquals("Perro", resultado.getEspecie()),
                () -> assertEquals("Labrador", resultado.getRaza()),
                () -> assertEquals(5, resultado.getEdad()),
                () -> assertEquals("Macho", resultado.getSexo()),
                () -> assertEquals(18.5, resultado.getPeso()),
                () -> assertEquals("CHIP-001", resultado.getMicrochip()),
                () -> assertTrue(resultado.isActivo()),
                () -> assertNotNull(resultado.getCreatedAt())
        );

        verify(clienteClient).findById(1L);
        verify(repository).save(any(Mascota.class));
    }

    @Test
    void createDebeRechazarClienteConRespuestaNula() {

        when(clienteClient.findById(1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El cliente dueño no existe o no está disponible",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void createDebeRechazarClienteConRespuestaVacia() {

        when(clienteClient.findById(1L))
                .thenReturn(Collections.emptyMap());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El cliente dueño no existe o no está disponible",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void createDebeConvertirErrorFeignEnBusinessException() {

        when(clienteClient.findById(1L))
                .thenThrow(crearFeignException404());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El cliente dueño no existe o no está disponible",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void updateDebeBuscarMascotaValidarClienteYActualizar() {

        Mascota mascota = crearMascota();

        MascotaRequest request = new MascotaRequest(
                2L,
                "Firulais actualizado",
                "Perro",
                "Golden Retriever",
                6,
                "Macho",
                20.2,
                "CHIP-002"
        );

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mascota));

        when(clienteClient.findById(2L))
                .thenReturn(Map.of(
                        "id", 2L,
                        "nombre", "Nuevo dueño"
                ));

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mascota resultado = service.update(1L, request);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(2L, resultado.getClienteId()),
                () -> assertEquals(
                        "Firulais actualizado",
                        resultado.getNombre()
                ),
                () -> assertEquals(
                        "Golden Retriever",
                        resultado.getRaza()
                ),
                () -> assertEquals(6, resultado.getEdad()),
                () -> assertEquals(20.2, resultado.getPeso()),
                () -> assertEquals(
                        "CHIP-002",
                        resultado.getMicrochip()
                )
        );

        InOrder orden = inOrder(repository, clienteClient);

        orden.verify(repository)
                .findByIdAndActivoTrue(1L);

        orden.verify(clienteClient)
                .findById(2L);

        orden.verify(repository)
                .save(mascota);
    }

    @Test
    void updateNoDebeConsultarClienteSiMascotaNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(99L, crearRequest())
        );

        verifyNoInteractions(clienteClient);
        verify(repository, never()).save(any());
    }

    @Test
    void updateNoDebeGuardarSiClienteNoExiste() {

        Mascota mascota = crearMascota();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mascota));

        when(clienteClient.findById(1L))
                .thenThrow(crearFeignException404());

        assertThrows(
                BusinessException.class,
                () -> service.update(1L, crearRequest())
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deleteDebeRealizarEliminacionLogica() {

        Mascota mascota = crearMascota();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(mascota));

        service.delete(1L);

        ArgumentCaptor<Mascota> captor =
                ArgumentCaptor.forClass(Mascota.class);

        verify(repository).save(captor.capture());

        assertFalse(captor.getValue().isActivo());
    }

    @Test
    void deleteDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never()).save(any());
    }

    private MascotaRequest crearRequest() {
        return new MascotaRequest(
                1L,
                "Firulais",
                "Perro",
                "Labrador",
                5,
                "Macho",
                18.5,
                "CHIP-001"
        );
    }

    private Mascota crearMascota() {

        Mascota mascota = new Mascota();

        mascota.setId(1L);
        mascota.setClienteId(1L);
        mascota.setNombre("Firulais");
        mascota.setEspecie("Perro");
        mascota.setRaza("Labrador");
        mascota.setEdad(5);
        mascota.setSexo("Macho");
        mascota.setPeso(18.5);
        mascota.setMicrochip("CHIP-001");
        mascota.setActivo(true);

        return mascota;
    }

    private FeignException crearFeignException404() {

        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/v1/clientes/99",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Collections.emptyMap())
                .build();

        return FeignException.errorStatus(
                "ClienteClient#findById",
                response
        );
    }
}
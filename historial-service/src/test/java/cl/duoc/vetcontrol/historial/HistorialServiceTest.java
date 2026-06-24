package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.client.MascotaClient;
import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.exception.BusinessException;
import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.repository.HistorialRepository;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HistorialServiceTest {

    private HistorialRepository repository;
    private MascotaClient mascotaClient;
    private HistorialService service;

    @BeforeEach
    void setUp() {
        repository = mock(HistorialRepository.class);
        mascotaClient = mock(MascotaClient.class);

        service = new HistorialService(
                repository,
                mascotaClient
        );
    }

    @Test
    void findAllDebeRetornarHistorialesOrdenados() {
        when(repository.findAllByOrderByFechaDesc())
                .thenReturn(List.of(crearHistorial()));

        List<HistorialClinico> resultado =
                service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(
                "ATENCION",
                resultado.get(0).getTipo()
        );

        verify(repository)
                .findAllByOrderByFechaDesc();
    }

    @Test
    void findByIdDebeRetornarHistorial() {
        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(crearHistorial())
                );

        HistorialClinico resultado =
                service.findById(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        10L,
                        resultado.getMascotaId()
                ),
                () -> assertEquals(
                        "ATENCION",
                        resultado.getTipo()
                )
        );
    }

    @Test
    void findByIdDebeFallarCuandoNoExiste() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findById(99L)
                );

        assertEquals(
                "Historial no encontrado: 99",
                exception.getMessage()
        );
    }

    @Test
    void findByMascotaDebeRetornarRegistrosOrdenados() {
        when(repository
                .findByMascotaIdOrderByFechaDesc(10L))
                .thenReturn(List.of(crearHistorial()));

        List<HistorialClinico> resultado =
                service.findByMascota(10L);

        assertEquals(1, resultado.size());
        assertEquals(
                10L,
                resultado.get(0).getMascotaId()
        );

        verify(repository)
                .findByMascotaIdOrderByFechaDesc(10L);
    }

    @Test
    void createDebeGuardarHistorialCorrectamente() {
        configurarMascotaValida();

        HistorialRequest request =
                crearRequest();

        when(repository
                .findByTipoAndReferenciaExternaId(
                        "VACUNA",
                        50L
                ))
                .thenReturn(Optional.empty());

        when(repository.save(any(HistorialClinico.class)))
                .thenAnswer(invocation -> {
                    HistorialClinico historial =
                            invocation.getArgument(0);

                    historial.setId(1L);

                    return historial;
                });

        HistorialClinico resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        10L,
                        resultado.getMascotaId()
                ),
                () -> assertEquals(
                        request.fecha(),
                        resultado.getFecha()
                ),
                () -> assertEquals(
                        "VACUNA",
                        resultado.getTipo()
                ),
                () -> assertEquals(
                        "Vacuna antirrábica aplicada",
                        resultado.getDetalle()
                ),
                () -> assertEquals(
                        50L,
                        resultado.getReferenciaExternaId()
                )
        );

        verify(repository)
                .findByTipoAndReferenciaExternaId(
                        "VACUNA",
                        50L
                );

        verify(repository)
                .save(any(HistorialClinico.class));
    }

    @Test
    void createDebeNormalizarTipoYDetalle() {
        configurarMascotaValida();

        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "  vacuna  ",
                        "  Aplicación de vacuna  ",
                        null
                );

        when(repository.save(any(HistorialClinico.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        HistorialClinico resultado =
                service.create(request);

        assertEquals(
                "VACUNA",
                resultado.getTipo()
        );

        assertEquals(
                "Aplicación de vacuna",
                resultado.getDetalle()
        );

        verify(repository, never())
                .findByTipoAndReferenciaExternaId(
                        any(),
                        any()
                );
    }

    @Test
    void createDebeAceptarMascotaSinCampoActivo() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of(
                        "id",
                        10L
                ));

        when(repository.save(any(HistorialClinico.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        HistorialClinico resultado =
                service.create(
                        new HistorialRequest(
                                10L,
                                LocalDateTime.now().minusHours(1),
                                "OBSERVACION",
                                "Paciente estable",
                                null
                        )
                );

        assertNotNull(resultado);
        assertEquals(
                "OBSERVACION",
                resultado.getTipo()
        );
    }

    @Test
    void createDebeRechazarMascotaNula() {
        when(mascotaClient.findById(10L))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeRechazarMascotaVacia() {
        when(mascotaClient.findById(10L))
                .thenReturn(Collections.emptyMap());

        assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeRechazarMascotaInactiva() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", false
                ));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeConvertirErrorFeignDeMascota() {
        when(mascotaClient.findById(10L))
                .thenThrow(
                        crearFeign404(
                                "/api/v1/mascotas/10"
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarFechaFutura() {
        configurarMascotaValida();

        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().plusDays(1),
                        "VACUNA",
                        "Vacuna",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "La fecha del historial no puede estar en el futuro",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void createDebeRechazarTipoNoPermitido() {
        configurarMascotaValida();

        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "OTRO",
                        "Detalle",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "Tipo de historial no permitido",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void createDebeRechazarReferenciaDuplicada() {
        configurarMascotaValida();

        when(repository
                .findByTipoAndReferenciaExternaId(
                        "VACUNA",
                        50L
                ))
                .thenReturn(
                        Optional.of(crearHistorial())
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "Ya existe un historial del tipo VACUNA con referencia externa 50",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void registrarDesdeAtencionDebeCrearHistorialAutomatico() {
        when(repository
                .findByTipoAndReferenciaExternaId(
                        "ATENCION",
                        500L
                ))
                .thenReturn(Optional.empty());

        when(repository.save(any(HistorialClinico.class)))
                .thenAnswer(invocation -> {
                    HistorialClinico historial =
                            invocation.getArgument(0);

                    historial.setId(1L);

                    return historial;
                });

        HistorialClinico resultado =
                service.registrarDesdeAtencion(
                        10L,
                        500L
                );

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        10L,
                        resultado.getMascotaId()
                ),
                () -> assertEquals(
                        "ATENCION",
                        resultado.getTipo()
                ),
                () -> assertEquals(
                        500L,
                        resultado.getReferenciaExternaId()
                ),
                () -> assertNotNull(resultado.getFecha()),
                () -> assertEquals(
                        "Atención veterinaria registrada automáticamente desde Kafka",
                        resultado.getDetalle()
                )
        );

        verify(repository)
                .save(any(HistorialClinico.class));
    }

    @Test
    void registrarDesdeAtencionDebeIgnorarEventoDuplicado() {
        HistorialClinico existente =
                crearHistorial();

        existente.setReferenciaExternaId(500L);

        when(repository
                .findByTipoAndReferenciaExternaId(
                        "ATENCION",
                        500L
                ))
                .thenReturn(Optional.of(existente));

        HistorialClinico resultado =
                service.registrarDesdeAtencion(
                        10L,
                        500L
                );

        assertSame(existente, resultado);

        verify(repository, never())
                .save(any());
    }

    @Test
    void registrarDesdeAtencionDebeRechazarMascotaIdNulo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeAtencion(
                                null,
                                500L
                        )
                );

        assertEquals(
                "mascotaId debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void registrarDesdeAtencionDebeRechazarMascotaIdCero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeAtencion(
                                0L,
                                500L
                        )
                );

        assertEquals(
                "mascotaId debe ser mayor que cero",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeAtencionDebeRechazarAtencionIdNulo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeAtencion(
                                10L,
                                null
                        )
                );

        assertEquals(
                "atencionId debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void registrarDesdeAtencionDebeRechazarAtencionIdNegativo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeAtencion(
                                10L,
                                -1L
                        )
                );

        assertEquals(
                "atencionId debe ser mayor que cero",
                exception.getMessage()
        );
    }

    private void configurarMascotaValida() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", true
                ));
    }

    private HistorialRequest crearRequest() {
        return new HistorialRequest(
                10L,
                LocalDateTime.now().minusHours(1),
                "VACUNA",
                "Vacuna antirrábica aplicada",
                50L
        );
    }

    private HistorialClinico crearHistorial() {
        HistorialClinico historial =
                new HistorialClinico();

        historial.setId(1L);
        historial.setMascotaId(10L);
        historial.setFecha(
                LocalDateTime.now().minusHours(1)
        );
        historial.setTipo("ATENCION");
        historial.setDetalle(
                "Control veterinario"
        );
        historial.setReferenciaExternaId(500L);

        return historial;
    }

    private FeignException crearFeign404(
            String url
    ) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                url,
                Collections
                        .<String, Collection<String>>
                                emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        Response response =
                Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .request(request)
                        .headers(
                                Collections
                                        .<String, Collection<String>>
                                                emptyMap()
                        )
                        .build();

        return FeignException.errorStatus(
                "MascotaClient#findById",
                response
        );
    }
}
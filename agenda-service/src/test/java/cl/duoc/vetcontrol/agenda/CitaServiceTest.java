package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.client.MascotaClient;
import cl.duoc.vetcontrol.agenda.client.VeterinarioClient;
import cl.duoc.vetcontrol.agenda.config.KafkaConfig;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import cl.duoc.vetcontrol.agenda.repository.CitaRepository;
import cl.duoc.vetcontrol.agenda.service.CitaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CitaServiceTest {

    private CitaRepository repository;
    private MascotaClient mascotaClient;
    private VeterinarioClient veterinarioClient;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private CitaService service;

    @BeforeEach
    void setUp() {
        repository = mock(CitaRepository.class);
        mascotaClient = mock(MascotaClient.class);
        veterinarioClient = mock(VeterinarioClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        service = new CitaService(
                repository,
                mascotaClient,
                veterinarioClient,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void findAllDebeExcluirCitasCanceladas() {
        when(repository.findByEstadoNot(EstadoCita.CANCELADA))
                .thenReturn(List.of(crearCita()));

        List<Cita> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(EstadoCita.PROGRAMADA, resultado.get(0).getEstado());

        verify(repository).findByEstadoNot(EstadoCita.CANCELADA);
    }

    @Test
    void findByIdDebeRetornarCitaNoCancelada() {
        when(repository.findByIdAndEstadoNot(
                1L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.of(crearCita()));

        Cita resultado = service.findById(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado())
        );
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {
        when(repository.findByIdAndEstadoNot(
                99L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(99L)
        );

        assertEquals("Cita no encontrada: 99", exception.getMessage());
    }

    @Test
    void byFechaDebeExcluirCanceladas() {
        LocalDate fecha = LocalDate.now().plusDays(1);

        when(repository.findByFechaAndEstadoNot(
                fecha,
                EstadoCita.CANCELADA
        )).thenReturn(List.of(crearCita()));

        List<Cita> resultado = service.byFecha(fecha);

        assertEquals(1, resultado.size());

        verify(repository).findByFechaAndEstadoNot(
                fecha,
                EstadoCita.CANCELADA
        );
    }

    @Test
    void byMascotaDebeExcluirCanceladas() {
        when(repository.findByMascotaIdAndEstadoNot(
                10L,
                EstadoCita.CANCELADA
        )).thenReturn(List.of(crearCita()));

        List<Cita> resultado = service.byMascota(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getMascotaId());
    }

    @Test
    void byVeterinarioDebeExcluirCanceladas() {
        when(repository.findByVeterinarioIdAndEstadoNot(
                20L,
                EstadoCita.CANCELADA
        )).thenReturn(List.of(crearCita()));

        List<Cita> resultado = service.byVeterinario(20L);

        assertEquals(1, resultado.size());
        assertEquals(20L, resultado.get(0).getVeterinarioId());
    }

    @Test
    void createDebeGuardarTodosLosCamposYPublicarEvento()
            throws Exception {

        configurarExternosValidos();

        CitaRequest request = crearRequest();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        20L,
                        request.fecha(),
                        request.hora(),
                        EstadoCita.CANCELADA
                ))
                .thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> {
                    Cita cita = invocation.getArgument(0);
                    cita.setId(1L);
                    return cita;
                });

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"tipo\":\"CITA_CREADA\"}");

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_CITA_CREADA),
                anyString()
        )).thenReturn(future);

        Cita resultado = service.create(request);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(10L, resultado.getMascotaId()),
                () -> assertEquals(20L, resultado.getVeterinarioId()),
                () -> assertEquals(request.fecha(), resultado.getFecha()),
                () -> assertEquals(request.hora(), resultado.getHora()),
                () -> assertEquals("Control veterinario", resultado.getMotivo()),
                () -> assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado())
        );

        ArgumentCaptor<Object> eventoCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(objectMapper)
                .writeValueAsString(eventoCaptor.capture());

        assertInstanceOf(Map.class, eventoCaptor.getValue());

        Map<?, ?> evento = (Map<?, ?>) eventoCaptor.getValue();

        assertAll(
                () -> assertEquals("CITA_CREADA", evento.get("tipo")),
                () -> assertEquals(1L, evento.get("citaId")),
                () -> assertEquals(10L, evento.get("mascotaId")),
                () -> assertEquals(20L, evento.get("veterinarioId")),
                () -> assertEquals(request.fecha(), evento.get("fecha")),
                () -> assertEquals(request.hora(), evento.get("hora"))
        );

        verify(kafkaTemplate).send(
                KafkaConfig.TOPIC_CITA_CREADA,
                "{\"tipo\":\"CITA_CREADA\"}"
        );
    }

    @Test
    void createDebeAceptarRecursosSinCampoActivo()
            throws Exception {

        when(mascotaClient.findById(10L))
                .thenReturn(Map.of("id", 10L));

        when(veterinarioClient.findById(20L))
                .thenReturn(Map.of("id", 20L));

        CitaRequest request = crearRequest();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        20L,
                        request.fecha(),
                        request.hora(),
                        EstadoCita.CANCELADA
                ))
                .thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> {
                    Cita cita = invocation.getArgument(0);
                    cita.setId(1L);
                    return cita;
                });

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        Cita resultado = service.create(request);

        assertNotNull(resultado);
    }

    @Test
    void createDebeRechazarFechaPasada() {
        CitaRequest request = new CitaRequest(
                10L,
                20L,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Control"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "La fecha de la cita no puede estar en el pasado",
                exception.getMessage()
        );

        verifyNoInteractions(
                mascotaClient,
                veterinarioClient,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void createDebeRechazarHoraPasadaDelMismoDia() {
        CitaRequest request = new CitaRequest(
                10L,
                20L,
                LocalDate.now(),
                LocalTime.MIN,
                "Control"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "La hora de la cita debe ser posterior a la hora actual",
                exception.getMessage()
        );
    }

    @Test
    void createDebeAceptarHoraFuturaDelMismoDia()
            throws Exception {

        configurarExternosValidos();

        CitaRequest request = new CitaRequest(
                10L,
                20L,
                LocalDate.now(),
                LocalTime.MAX,
                "Control"
        );

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        anyLong(),
                        any(LocalDate.class),
                        any(LocalTime.class),
                        eq(EstadoCita.CANCELADA)
                ))
                .thenReturn(false);

        Cita cita = crearCita();
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());

        when(repository.save(any(Cita.class)))
                .thenReturn(cita);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertNotNull(service.create(request));
    }

    @Test
    void createDebeRechazarBloqueHorarioOcupado() {
        configurarExternosValidos();

        CitaRequest request = crearRequest();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        20L,
                        request.fecha(),
                        request.hora(),
                        EstadoCita.CANCELADA
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "El veterinario ya tiene una cita en ese bloque horario",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void createDebeRechazarMascotaNula() {
        when(mascotaClient.findById(10L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(veterinarioClient);
    }

    @Test
    void createDebeRechazarMascotaVacia() {
        when(mascotaClient.findById(10L))
                .thenReturn(Collections.emptyMap());

        assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        verifyNoInteractions(veterinarioClient);
    }

    @Test
    void createDebeRechazarMascotaInactiva() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeConvertirErrorFeignDeMascota() {
        when(mascotaClient.findById(10L))
                .thenThrow(crearFeign404("/api/v1/mascotas/10"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "La mascota no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarVeterinarioNulo() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of("id", 10L, "activo", true));

        when(veterinarioClient.findById(20L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El veterinario no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarVeterinarioVacio() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of("id", 10L, "activo", true));

        when(veterinarioClient.findById(20L))
                .thenReturn(Collections.emptyMap());

        assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );
    }

    @Test
    void createDebeRechazarVeterinarioInactivo() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of("id", 10L, "activo", true));

        when(veterinarioClient.findById(20L))
                .thenReturn(Map.of(
                        "id", 20L,
                        "activo", false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El veterinario no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeConvertirErrorFeignDeVeterinario() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of("id", 10L, "activo", true));

        when(veterinarioClient.findById(20L))
                .thenThrow(
                        crearFeign404("/api/v1/veterinarios/20")
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequest())
        );

        assertEquals(
                "El veterinario no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeContinuarSiKafkaFallaAsincronamente()
            throws Exception {

        configurarCreacionValida();

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        CompletableFuture<SendResult<String, String>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException("Kafka no disponible")
        );

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_CITA_CREADA),
                anyString()
        )).thenReturn(future);

        Cita resultado = service.create(crearRequest());

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void createDebeContinuarSiJsonFalla()
            throws Exception {

        configurarCreacionValida();

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(
                        new JsonProcessingException("Error JSON") {
                        }
                );

        Cita resultado = service.create(crearRequest());

        assertNotNull(resultado);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createDebeContinuarSiKafkaLanzaExcepcionDirecta()
            throws Exception {

        configurarCreacionValida();

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_CITA_CREADA),
                anyString()
        )).thenThrow(
                new RuntimeException("Kafka detenido")
        );

        Cita resultado = service.create(crearRequest());

        assertNotNull(resultado);
    }

    @Test
    void updateDebeModificarTodosLosCampos() {
        Cita existente = crearCita();
        CitaRequest request = crearRequestActualizado();

        when(repository.findByIdAndEstadoNot(
                1L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.of(existente));

        configurarExternosValidosActualizados();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
                        21L,
                        request.fecha(),
                        request.hora(),
                        1L,
                        EstadoCita.CANCELADA
                ))
                .thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cita resultado = service.update(1L, request);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(11L, resultado.getMascotaId()),
                () -> assertEquals(21L, resultado.getVeterinarioId()),
                () -> assertEquals(request.fecha(), resultado.getFecha()),
                () -> assertEquals(request.hora(), resultado.getHora()),
                () -> assertEquals("Vacunación", resultado.getMotivo()),
                () -> assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado())
        );

        verify(repository).save(existente);
        verifyNoInteractions(kafkaTemplate, objectMapper);
    }

    @Test
    void updateDebeRechazarHorarioOcupado() {
        when(repository.findByIdAndEstadoNot(
                1L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.of(crearCita()));

        configurarExternosValidosActualizados();

        CitaRequest request = crearRequestActualizado();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
                        21L,
                        request.fecha(),
                        request.hora(),
                        1L,
                        EstadoCita.CANCELADA
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.update(1L, request)
        );

        assertEquals(
                "El veterinario ya tiene una cita en ese bloque horario",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void updateDebeFallarAntesDeValidarExternosSiNoExiste() {
        when(repository.findByIdAndEstadoNot(
                99L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(99L, crearRequest())
        );

        verifyNoInteractions(
                mascotaClient,
                veterinarioClient,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void deleteDebeCancelarCitaLogicamente() {
        Cita cita = crearCita();

        when(repository.findByIdAndEstadoNot(
                1L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.of(cita));

        service.delete(1L);

        ArgumentCaptor<Cita> captor =
                ArgumentCaptor.forClass(Cita.class);

        verify(repository).save(captor.capture());

        assertEquals(
                EstadoCita.CANCELADA,
                captor.getValue().getEstado()
        );
    }

    @Test
    void deleteDebeFallarSiNoExiste() {
        when(repository.findByIdAndEstadoNot(
                99L,
                EstadoCita.CANCELADA
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never()).save(any());
    }

    private void configurarExternosValidos() {
        when(mascotaClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", true
                ));

        when(veterinarioClient.findById(20L))
                .thenReturn(Map.of(
                        "id", 20L,
                        "activo", true
                ));
    }

    private void configurarExternosValidosActualizados() {
        when(mascotaClient.findById(11L))
                .thenReturn(Map.of(
                        "id", 11L,
                        "activo", true
                ));

        when(veterinarioClient.findById(21L))
                .thenReturn(Map.of(
                        "id", 21L,
                        "activo", true
                ));
    }

    private void configurarCreacionValida() {
        configurarExternosValidos();

        CitaRequest request = crearRequest();

        when(repository
                .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        20L,
                        request.fecha(),
                        request.hora(),
                        EstadoCita.CANCELADA
                ))
                .thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenReturn(crearCita());
    }

    private CitaRequest crearRequest() {
        return new CitaRequest(
                10L,
                20L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Control veterinario"
        );
    }

    private CitaRequest crearRequestActualizado() {
        return new CitaRequest(
                11L,
                21L,
                LocalDate.now().plusDays(2),
                LocalTime.of(12, 0),
                "Vacunación"
        );
    }

    private Cita crearCita() {
        Cita cita = new Cita();

        cita.setId(1L);
        cita.setMascotaId(10L);
        cita.setVeterinarioId(20L);
        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        cita.setMotivo("Control veterinario");
        cita.setEstado(EstadoCita.PROGRAMADA);

        return cita;
    }

    private FeignException crearFeign404(String url) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                url,
                Collections.<String, Collection<String>>emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(
                        Collections
                                .<String, Collection<String>>emptyMap()
                )
                .build();

        return FeignException.errorStatus(
                "ClienteFeign#findById",
                response
        );
    }
}
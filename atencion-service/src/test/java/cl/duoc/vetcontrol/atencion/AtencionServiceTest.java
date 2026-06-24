package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.config.KafkaConfig;
import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.exception.BusinessException;
import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.repository.AtencionRepository;
import cl.duoc.vetcontrol.atencion.service.AtencionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AtencionServiceTest {

    private AtencionRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private AtencionService service;

    @BeforeEach
    void setUp() {
        repository = mock(AtencionRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        service = new AtencionService(
                repository,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void findAllDebeRetornarAtencionesActivas() {

        when(repository.findByActivoTrue())
                .thenReturn(List.of(crearAtencion()));

        List<Atencion> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(
                "Dermatitis",
                resultado.get(0).getDiagnostico()
        );

        verify(repository).findByActivoTrue();
    }

    @Test
    void findByIdDebeRetornarAtencionActiva() {

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(crearAtencion()));

        Atencion resultado = service.findById(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertTrue(resultado.isActivo()),
                () -> assertEquals(
                        "Dermatitis",
                        resultado.getDiagnostico()
                )
        );

        verify(repository).findByIdAndActivoTrue(1L);
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
                "Atención no encontrada: 99",
                exception.getMessage()
        );
    }

    @Test
    void byMascotaDebeRetornarResultadosActivos() {

        when(repository.findByMascotaIdAndActivoTrue(10L))
                .thenReturn(List.of(crearAtencion()));

        List<Atencion> resultado =
                service.byMascota(10L);

        assertEquals(1, resultado.size());
        assertEquals(
                10L,
                resultado.get(0).getMascotaId()
        );

        verify(repository)
                .findByMascotaIdAndActivoTrue(10L);
    }

    @Test
    void byVeterinarioDebeRetornarResultadosActivos() {

        when(repository
                .findByVeterinarioIdAndActivoTrue(20L))
                .thenReturn(List.of(crearAtencion()));

        List<Atencion> resultado =
                service.byVeterinario(20L);

        assertEquals(1, resultado.size());
        assertEquals(
                20L,
                resultado.get(0).getVeterinarioId()
        );

        verify(repository)
                .findByVeterinarioIdAndActivoTrue(20L);
    }

    @Test
    void createDebeGuardarYPublicarEventoCorrectamente()
            throws Exception {

        AtencionRequest request = crearRequest();

        when(repository.existsByCitaIdAndActivoTrue(5L))
                .thenReturn(false);

        when(repository.save(any(Atencion.class)))
                .thenAnswer(invocation -> {
                    Atencion atencion =
                            invocation.getArgument(0);

                    atencion.setId(1L);

                    return atencion;
                });

        when(objectMapper.writeValueAsString(any()))
                .thenReturn(
                        "{\"tipo\":\"ATENCION_REGISTRADA\"}"
                );

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_ATENCION_REGISTRADA),
                anyString()
        )).thenReturn(future);

        Atencion resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        5L,
                        resultado.getCitaId()
                ),
                () -> assertEquals(
                        10L,
                        resultado.getMascotaId()
                ),
                () -> assertEquals(
                        20L,
                        resultado.getVeterinarioId()
                ),
                () -> assertEquals(
                        request.fechaAtencion(),
                        resultado.getFechaAtencion()
                ),
                () -> assertEquals(
                        "Dermatitis",
                        resultado.getDiagnostico()
                ),
                () -> assertEquals(
                        "Antihistamínico",
                        resultado.getTratamiento()
                ),
                () -> assertEquals(
                        "Control en siete días",
                        resultado.getObservaciones()
                ),
                () -> assertTrue(resultado.isActivo())
        );

        ArgumentCaptor<Object> eventoCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(objectMapper)
                .writeValueAsString(
                        eventoCaptor.capture()
                );

        assertInstanceOf(
                Map.class,
                eventoCaptor.getValue()
        );

        Map<?, ?> evento =
                (Map<?, ?>) eventoCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        "ATENCION_REGISTRADA",
                        evento.get("tipo")
                ),
                () -> assertEquals(
                        1L,
                        evento.get("atencionId")
                ),
                () -> assertEquals(
                        5L,
                        evento.get("citaId")
                ),
                () -> assertEquals(
                        10L,
                        evento.get("mascotaId")
                ),
                () -> assertEquals(
                        20L,
                        evento.get("veterinarioId")
                )
        );

        verify(kafkaTemplate).send(
                KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                "{\"tipo\":\"ATENCION_REGISTRADA\"}"
        );

        verify(repository).save(any(Atencion.class));
    }

    @Test
    void createDebeRechazarCitaDuplicada() {

        when(repository.existsByCitaIdAndActivoTrue(5L))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "Ya existe una atención registrada para la cita: 5",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any(Atencion.class));

        verifyNoInteractions(
                objectMapper,
                kafkaTemplate
        );
    }

    @Test
    void createDebeContinuarCuandoKafkaFalla()
            throws Exception {

        when(repository.existsByCitaIdAndActivoTrue(5L))
                .thenReturn(false);

        when(repository.save(any(Atencion.class)))
                .thenReturn(crearAtencion());

        when(objectMapper.writeValueAsString(any()))
                .thenReturn(
                        "{\"tipo\":\"ATENCION_REGISTRADA\"}"
                );

        CompletableFuture<SendResult<String, String>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException(
                        "Kafka no disponible"
                )
        );

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_ATENCION_REGISTRADA),
                anyString()
        )).thenReturn(future);

        Atencion resultado =
                service.create(crearRequest());

        assertAll(
                () -> assertNotNull(resultado),
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        5L,
                        resultado.getCitaId()
                )
        );

        verify(kafkaTemplate).send(
                KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                "{\"tipo\":\"ATENCION_REGISTRADA\"}"
        );
    }

    @Test
    void createDebeContinuarCuandoSerializacionJsonFalla()
            throws Exception {

        when(repository.existsByCitaIdAndActivoTrue(5L))
                .thenReturn(false);

        when(repository.save(any(Atencion.class)))
                .thenReturn(crearAtencion());

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(
                        new JsonProcessingException(
                                "Error JSON"
                        ) {
                        }
                );

        Atencion resultado =
                service.create(crearRequest());

        assertAll(
                () -> assertNotNull(resultado),
                () -> assertEquals(
                        1L,
                        resultado.getId()
                )
        );

        verify(objectMapper)
                .writeValueAsString(any());

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createDebeContinuarCuandoKafkaLanzaExcepcionDirecta()
            throws Exception {

        when(repository.existsByCitaIdAndActivoTrue(5L))
                .thenReturn(false);

        when(repository.save(any(Atencion.class)))
                .thenReturn(crearAtencion());

        when(objectMapper.writeValueAsString(any()))
                .thenReturn(
                        "{\"tipo\":\"ATENCION_REGISTRADA\"}"
                );

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_ATENCION_REGISTRADA),
                anyString()
        )).thenThrow(
                new RuntimeException(
                        "Kafka detenido"
                )
        );

        Atencion resultado =
                service.create(crearRequest());

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());

        verify(repository).save(any(Atencion.class));
    }

    @Test
    void updateDebeModificarTodosLosCampos() {

        Atencion existente = crearAtencion();

        LocalDateTime nuevaFecha =
                LocalDateTime.now().minusMinutes(10);

        AtencionRequest request =
                new AtencionRequest(
                        6L,
                        11L,
                        21L,
                        nuevaFecha,
                        "Diagnóstico actualizado",
                        "Tratamiento actualizado",
                        "Observación actualizada"
                );

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(existente));

        when(repository
                .existsByCitaIdAndIdNotAndActivoTrue(
                        6L,
                        1L
                ))
                .thenReturn(false);

        when(repository.save(any(Atencion.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Atencion resultado =
                service.update(1L, request);

        assertAll(
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        6L,
                        resultado.getCitaId()
                ),
                () -> assertEquals(
                        11L,
                        resultado.getMascotaId()
                ),
                () -> assertEquals(
                        21L,
                        resultado.getVeterinarioId()
                ),
                () -> assertEquals(
                        nuevaFecha,
                        resultado.getFechaAtencion()
                ),
                () -> assertEquals(
                        "Diagnóstico actualizado",
                        resultado.getDiagnostico()
                ),
                () -> assertEquals(
                        "Tratamiento actualizado",
                        resultado.getTratamiento()
                ),
                () -> assertEquals(
                        "Observación actualizada",
                        resultado.getObservaciones()
                ),
                () -> assertTrue(resultado.isActivo())
        );

        verify(repository)
                .existsByCitaIdAndIdNotAndActivoTrue(
                        6L,
                        1L
                );

        verify(repository).save(existente);

        verifyNoInteractions(
                objectMapper,
                kafkaTemplate
        );
    }

    @Test
    void updateDebeRechazarCitaDeOtraAtencion() {

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(crearAtencion()));

        when(repository
                .existsByCitaIdAndIdNotAndActivoTrue(
                        5L,
                        1L
                ))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(
                                1L,
                                crearRequest()
                        )
                );

        assertEquals(
                "Ya existe otra atención registrada para la cita: 5",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any(Atencion.class));

        verifyNoInteractions(
                objectMapper,
                kafkaTemplate
        );
    }

    @Test
    void updateDebeFallarCuandoAtencionNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.update(
                                99L,
                                crearRequest()
                        )
                );

        assertEquals(
                "Atención no encontrada: 99",
                exception.getMessage()
        );

        verify(repository, never())
                .existsByCitaIdAndIdNotAndActivoTrue(
                        anyLong(),
                        anyLong()
                );

        verify(repository, never())
                .save(any(Atencion.class));
    }

    @Test
    void deleteDebeRealizarEliminacionLogica() {

        Atencion atencion = crearAtencion();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(atencion));

        service.delete(1L);

        ArgumentCaptor<Atencion> captor =
                ArgumentCaptor.forClass(
                        Atencion.class
                );

        verify(repository).save(
                captor.capture()
        );

        assertAll(
                () -> assertEquals(
                        1L,
                        captor.getValue().getId()
                ),
                () -> assertFalse(
                        captor.getValue().isActivo()
                )
        );

        verifyNoInteractions(
                objectMapper,
                kafkaTemplate
        );
    }

    @Test
    void deleteDebeFallarCuandoAtencionNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.delete(99L)
                );

        assertEquals(
                "Atención no encontrada: 99",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any(Atencion.class));

        verifyNoInteractions(
                objectMapper,
                kafkaTemplate
        );
    }

    private AtencionRequest crearRequest() {
        return new AtencionRequest(
                5L,
                10L,
                20L,
                LocalDateTime.now().minusHours(1),
                "Dermatitis",
                "Antihistamínico",
                "Control en siete días"
        );
    }

    private Atencion crearAtencion() {

        Atencion atencion = new Atencion();

        atencion.setId(1L);
        atencion.setCitaId(5L);
        atencion.setMascotaId(10L);
        atencion.setVeterinarioId(20L);
        atencion.setFechaAtencion(
                LocalDateTime.now().minusHours(1)
        );
        atencion.setDiagnostico("Dermatitis");
        atencion.setTratamiento("Antihistamínico");
        atencion.setObservaciones(
                "Control en siete días"
        );
        atencion.setActivo(true);

        return atencion;
    }
}
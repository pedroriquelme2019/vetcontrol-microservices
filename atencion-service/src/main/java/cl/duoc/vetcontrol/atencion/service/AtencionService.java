package cl.duoc.vetcontrol.atencion.service;

import cl.duoc.vetcontrol.atencion.config.KafkaConfig;
import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.exception.BusinessException;
import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.repository.AtencionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AtencionService {

    private static final Logger log =
            LoggerFactory.getLogger(AtencionService.class);

    private static final String TIPO_EVENTO =
            "ATENCION_REGISTRADA";

    private final AtencionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AtencionService(
            AtencionRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Atencion> findAll() {
        return repository.findByActivoTrue();
    }

    public Atencion findById(Long id) {
        return repository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Atención no encontrada: " + id
                        )
                );
    }

    public List<Atencion> byMascota(Long mascotaId) {
        return repository
                .findByMascotaIdAndActivoTrue(mascotaId);
    }

    public List<Atencion> byVeterinario(
            Long veterinarioId
    ) {
        return repository
                .findByVeterinarioIdAndActivoTrue(
                        veterinarioId
                );
    }

    public Atencion create(AtencionRequest request) {

        validarCitaDisponibleParaCrear(
                request.citaId()
        );

        Atencion atencion = map(
                new Atencion(),
                request
        );

        Atencion guardada =
                repository.save(atencion);

        publicarEventoAtencionRegistrada(
                guardada
        );

        log.info(
                "Atención creada id={} mascota={} cita={}",
                guardada.getId(),
                guardada.getMascotaId(),
                guardada.getCitaId()
        );

        return guardada;
    }

    public Atencion update(
            Long id,
            AtencionRequest request
    ) {
        Atencion atencion = findById(id);

        validarCitaDisponibleParaActualizar(
                request.citaId(),
                id
        );

        map(atencion, request);

        Atencion actualizada =
                repository.save(atencion);

        log.info(
                "Atención {} actualizada",
                id
        );

        return actualizada;
    }

    public void delete(Long id) {

        Atencion atencion = findById(id);

        atencion.setActivo(false);

        repository.save(atencion);

        log.info(
                "Atención {} marcada como inactiva",
                id
        );
    }

    private void validarCitaDisponibleParaCrear(
            Long citaId
    ) {
        if (repository.existsByCitaIdAndActivoTrue(
                citaId
        )) {
            throw new BusinessException(
                    "Ya existe una atención registrada para la cita: "
                            + citaId
            );
        }
    }

    private void validarCitaDisponibleParaActualizar(
            Long citaId,
            Long atencionId
    ) {
        if (repository
                .existsByCitaIdAndIdNotAndActivoTrue(
                        citaId,
                        atencionId
                )) {
            throw new BusinessException(
                    "Ya existe otra atención registrada para la cita: "
                            + citaId
            );
        }
    }

    private void publicarEventoAtencionRegistrada(
            Atencion atencion
    ) {
        try {
            Map<String, Object> evento =
                    construirEvento(atencion);

            String mensaje =
                    objectMapper.writeValueAsString(
                            evento
                    );

            kafkaTemplate
                    .send(
                            KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                            mensaje
                    )
                    .whenComplete(
                            (resultado, error) -> {

                                if (error != null) {
                                    log.error(
                                            "Kafka no pudo publicar la atención {}",
                                            atencion.getId(),
                                            error
                                    );

                                    return;
                                }

                                log.info(
                                        "Evento Kafka publicado para atención {}",
                                        atencion.getId()
                                );
                            }
                    );

        } catch (Exception exception) {
            log.error(
                    "No fue posible construir o publicar el evento de la atención {}",
                    atencion.getId(),
                    exception
            );
        }
    }

    private Map<String, Object> construirEvento(
            Atencion atencion
    ) {
        Map<String, Object> evento =
                new LinkedHashMap<>();

        evento.put(
                "tipo",
                TIPO_EVENTO
        );

        evento.put(
                "atencionId",
                atencion.getId()
        );

        evento.put(
                "citaId",
                atencion.getCitaId()
        );

        evento.put(
                "mascotaId",
                atencion.getMascotaId()
        );

        evento.put(
                "veterinarioId",
                atencion.getVeterinarioId()
        );

        return evento;
    }

    private Atencion map(
            Atencion atencion,
            AtencionRequest request
    ) {
        atencion.setCitaId(
                request.citaId()
        );

        atencion.setMascotaId(
                request.mascotaId()
        );

        atencion.setVeterinarioId(
                request.veterinarioId()
        );

        atencion.setFechaAtencion(
                request.fechaAtencion()
        );

        atencion.setDiagnostico(
                request.diagnostico()
        );

        atencion.setTratamiento(
                request.tratamiento()
        );

        atencion.setObservaciones(
                request.observaciones()
        );

        return atencion;
    }
}


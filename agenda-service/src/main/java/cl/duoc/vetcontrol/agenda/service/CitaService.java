package cl.duoc.vetcontrol.agenda.service;

import cl.duoc.vetcontrol.agenda.client.MascotaClient;
import cl.duoc.vetcontrol.agenda.client.VeterinarioClient;
import cl.duoc.vetcontrol.agenda.config.KafkaConfig;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import cl.duoc.vetcontrol.agenda.repository.CitaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CitaService {

    private static final Logger log =
            LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository repository;
    private final MascotaClient mascotaClient;
    private final VeterinarioClient veterinarioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CitaService(
            CitaRepository repository,
            MascotaClient mascotaClient,
            VeterinarioClient veterinarioClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.mascotaClient = mascotaClient;
        this.veterinarioClient = veterinarioClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Cita> findAll() {
        return repository.findByEstadoNot(
                EstadoCita.CANCELADA
        );
    }

    public Cita findById(Long id) {
        return repository
                .findByIdAndEstadoNot(
                        id,
                        EstadoCita.CANCELADA
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cita no encontrada: " + id
                        )
                );
    }

    public List<Cita> byFecha(LocalDate fecha) {
        return repository.findByFechaAndEstadoNot(
                fecha,
                EstadoCita.CANCELADA
        );
    }

    public List<Cita> byMascota(Long mascotaId) {
        return repository
                .findByMascotaIdAndEstadoNot(
                        mascotaId,
                        EstadoCita.CANCELADA
                );
    }

    public List<Cita> byVeterinario(
            Long veterinarioId
    ) {
        return repository
                .findByVeterinarioIdAndEstadoNot(
                        veterinarioId,
                        EstadoCita.CANCELADA
                );
    }

    public Cita create(CitaRequest request) {

        validarFechaYHora(request);
        validarRecursosExternos(request);
        validarDisponibilidad(request, null);

        Cita cita = map(
                new Cita(),
                request
        );

        Cita guardada =
                repository.save(cita);

        publicarEventoCitaCreada(guardada);

        log.info(
                "Cita creada id={} veterinario={} fecha={} hora={}",
                guardada.getId(),
                guardada.getVeterinarioId(),
                guardada.getFecha(),
                guardada.getHora()
        );

        return guardada;
    }

    public Cita update(
            Long id,
            CitaRequest request
    ) {
        Cita cita = findById(id);

        validarFechaYHora(request);
        validarRecursosExternos(request);
        validarDisponibilidad(request, id);

        map(cita, request);

        Cita actualizada =
                repository.save(cita);

        log.info(
                "Cita {} actualizada",
                id
        );

        return actualizada;
    }

    public void delete(Long id) {

        Cita cita = findById(id);

        cita.setEstado(
                EstadoCita.CANCELADA
        );

        repository.save(cita);

        log.info(
                "Cita {} cancelada",
                id
        );
    }

    private void validarFechaYHora(
            CitaRequest request
    ) {
        LocalDate fechaActual =
                LocalDate.now();

        LocalTime horaActual =
                LocalTime.now();

        if (request.fecha().isBefore(fechaActual)) {
            throw new BusinessException(
                    "La fecha de la cita no puede estar en el pasado"
            );
        }

        if (request.fecha().isEqual(fechaActual)
                && !request.hora().isAfter(horaActual)) {

            throw new BusinessException(
                    "La hora de la cita debe ser posterior a la hora actual"
            );
        }
    }

    private void validarRecursosExternos(
            CitaRequest request
    ) {
        validarMascota(
                request.mascotaId()
        );

        validarVeterinario(
                request.veterinarioId()
        );
    }

    private void validarMascota(Long mascotaId) {
        try {
            Map<String, Object> mascota =
                    mascotaClient.findById(mascotaId);

            if (noDisponible(mascota)) {
                throw new BusinessException(
                        "La mascota no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "La mascota no existe o no está disponible"
            );
        }
    }

    private void validarVeterinario(
            Long veterinarioId
    ) {
        try {
            Map<String, Object> veterinario =
                    veterinarioClient.findById(
                            veterinarioId
                    );

            if (noDisponible(veterinario)) {
                throw new BusinessException(
                        "El veterinario no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "El veterinario no existe o no está disponible"
            );
        }
    }

    private boolean noDisponible(
            Map<String, Object> recurso
    ) {
        if (recurso == null || recurso.isEmpty()) {
            return true;
        }

        Object activo = recurso.get("activo");

        return activo instanceof Boolean
                && !((Boolean) activo);
    }

    private void validarDisponibilidad(
            CitaRequest request,
            Long citaId
    ) {
        boolean ocupado;

        if (citaId == null) {
            ocupado = repository
                    .existsByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                            request.veterinarioId(),
                            request.fecha(),
                            request.hora(),
                            EstadoCita.CANCELADA
                    );
        } else {
            ocupado = repository
                    .existsByVeterinarioIdAndFechaAndHoraAndIdNotAndEstadoNot(
                            request.veterinarioId(),
                            request.fecha(),
                            request.hora(),
                            citaId,
                            EstadoCita.CANCELADA
                    );
        }

        if (ocupado) {
            throw new BusinessException(
                    "El veterinario ya tiene una cita en ese bloque horario"
            );
        }
    }

    private void publicarEventoCitaCreada(
            Cita cita
    ) {
        try {
            Map<String, Object> evento =
                    new LinkedHashMap<>();

            evento.put(
                    "tipo",
                    "CITA_CREADA"
            );

            evento.put(
                    "citaId",
                    cita.getId()
            );

            evento.put(
                    "mascotaId",
                    cita.getMascotaId()
            );

            evento.put(
                    "veterinarioId",
                    cita.getVeterinarioId()
            );

            evento.put(
                    "fecha",
                    cita.getFecha()
            );

            evento.put(
                    "hora",
                    cita.getHora()
            );

            String mensaje =
                    objectMapper.writeValueAsString(
                            evento
                    );

            kafkaTemplate
                    .send(
                            KafkaConfig.TOPIC_CITA_CREADA,
                            mensaje
                    )
                    .whenComplete(
                            (resultado, error) -> {

                                if (error != null) {
                                    log.error(
                                            "Kafka no pudo publicar la cita {}",
                                            cita.getId(),
                                            error
                                    );

                                    return;
                                }

                                log.info(
                                        "Evento Kafka publicado para cita {}",
                                        cita.getId()
                                );
                            }
                    );

        } catch (Exception exception) {
            log.error(
                    "No fue posible publicar el evento de la cita {}",
                    cita.getId(),
                    exception
            );
        }
    }

    private Cita map(
            Cita cita,
            CitaRequest request
    ) {
        cita.setMascotaId(
                request.mascotaId()
        );

        cita.setVeterinarioId(
                request.veterinarioId()
        );

        cita.setFecha(
                request.fecha()
        );

        cita.setHora(
                request.hora()
        );

        cita.setMotivo(
                request.motivo()
        );

        return cita;
    }
}
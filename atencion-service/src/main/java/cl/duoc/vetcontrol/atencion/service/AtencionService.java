package cl.duoc.vetcontrol.atencion.service;

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

import java.util.List;
import java.util.Map;

@Service
public class AtencionService {

    private static final Logger log = LoggerFactory.getLogger(AtencionService.class);

    private final AtencionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AtencionService(AtencionRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Atencion> findAll() {
        return repository.findAll();
    }

    public Atencion findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención no encontrada: " + id));
    }

    public Atencion create(AtencionRequest request) {
        // CORREGIDO: valida que no exista ya una atención para la misma cita
        if (repository.existsByCitaId(request.citaId())) {
            throw new BusinessException("Ya existe una atención registrada para la cita: " + request.citaId());
        }

        Atencion saved = repository.save(map(new Atencion(), request));
        publicarEventoAtencionRegistrada(saved);

        log.info("Atención creada id={} mascota={} cita={}",
                saved.getId(), saved.getMascotaId(), saved.getCitaId());

        return saved;
    }

    public List<Atencion> byMascota(Long mascotaId) {
        return repository.findByMascotaId(mascotaId);
    }

    // --- privados ---

    private void publicarEventoAtencionRegistrada(Atencion atencion) {
        try {
            // CORREGIDO: JSON construido con ObjectMapper, no con concatenación de strings
            String evento = objectMapper.writeValueAsString(
                    Map.of("atencionId", atencion.getId(), "mascotaId", atencion.getMascotaId())
            );
            kafkaTemplate.send("atencion-registrada", evento);
        } catch (Exception ex) {
            log.error("Error al publicar evento atencion-registrada para atencionId={}", atencion.getId(), ex);
        }
    }

    private Atencion map(Atencion atencion, AtencionRequest request) {
        atencion.setCitaId(request.citaId());
        atencion.setMascotaId(request.mascotaId());
        atencion.setVeterinarioId(request.veterinarioId());
        atencion.setFechaAtencion(request.fechaAtencion());
        atencion.setDiagnostico(request.diagnostico());
        atencion.setTratamiento(request.tratamiento());
        atencion.setObservaciones(request.observaciones());
        return atencion;
    }
}

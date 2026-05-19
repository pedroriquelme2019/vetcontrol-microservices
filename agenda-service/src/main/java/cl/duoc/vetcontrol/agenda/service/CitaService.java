package cl.duoc.vetcontrol.agenda.service;

import cl.duoc.vetcontrol.agenda.client.MascotaClient;
import cl.duoc.vetcontrol.agenda.client.VeterinarioClient;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.repository.CitaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository repository;
    private final MascotaClient mascotaClient;
    private final VeterinarioClient veterinarioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CitaService(CitaRepository repository,
                       MascotaClient mascotaClient,
                       VeterinarioClient veterinarioClient,
                       KafkaTemplate<String, String> kafkaTemplate,
                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.mascotaClient = mascotaClient;
        this.veterinarioClient = veterinarioClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Cita> findAll() {
        return repository.findAll();
    }

    public Cita findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }

    public Cita create(CitaRequest request) {
        validarExternos(request);

        boolean ocupado = repository.existsByVeterinarioIdAndFechaAndHora(
                request.veterinarioId(),
                request.fecha(),
                request.hora()
        );

        if (ocupado) {
            throw new BusinessException("El veterinario ya tiene una cita en ese bloque horario");
        }

        Cita saved = repository.save(map(new Cita(), request));
        publicarEventoCitaCreada(saved);

        log.info("Cita creada id={} veterinario={} fecha={} hora={}",
                saved.getId(), saved.getVeterinarioId(), saved.getFecha(), saved.getHora());

        return saved;
    }

    public Cita update(Long id, CitaRequest request) {
        validarExternos(request);

        Cita cita = findById(id);

        boolean cambioHorario = !cita.getVeterinarioId().equals(request.veterinarioId())
                || !cita.getFecha().equals(request.fecha())
                || !cita.getHora().equals(request.hora());

        if (cambioHorario) {
            boolean ocupado = repository.existsByVeterinarioIdAndFechaAndHora(
                    request.veterinarioId(),
                    request.fecha(),
                    request.hora()
            );

            if (ocupado) {
                throw new BusinessException("El veterinario ya tiene una cita en ese bloque horario");
            }
        }

        return repository.save(map(cita, request));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    public List<Cita> byFecha(LocalDate fecha) {
        return repository.findByFecha(fecha);
    }

    // --- privados ---

    private void validarExternos(CitaRequest request) {
        try {
            mascotaClient.findById(request.mascotaId());
            veterinarioClient.findById(request.veterinarioId());
        } catch (FeignException ex) {
            throw new BusinessException("Mascota o veterinario no existe/no disponible");
        }
    }

    private void publicarEventoCitaCreada(Cita cita) {
        try {
            String evento = objectMapper.writeValueAsString(
                    Map.of("citaId", cita.getId(), "mascotaId", cita.getMascotaId())
            );
            kafkaTemplate.send("cita-creada", evento);
        } catch (Exception ex) {
            log.error("Error al publicar evento cita-creada para citaId={}", cita.getId(), ex);
        }
    }

    private Cita map(Cita cita, CitaRequest request) {
        cita.setMascotaId(request.mascotaId());
        cita.setVeterinarioId(request.veterinarioId());
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setMotivo(request.motivo());
        return cita;
    }
}

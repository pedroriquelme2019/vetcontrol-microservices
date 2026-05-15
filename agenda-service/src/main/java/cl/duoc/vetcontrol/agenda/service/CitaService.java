package cl.duoc.vetcontrol.agenda.service;

import cl.duoc.vetcontrol.agenda.client.MascotaClient;
import cl.duoc.vetcontrol.agenda.client.VeterinarioClient;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.repository.CitaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository repository;
    private final MascotaClient mascotaClient;
    private final VeterinarioClient veterinarioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CitaService(CitaRepository repository,
                       MascotaClient mascotaClient,
                       VeterinarioClient veterinarioClient,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.mascotaClient = mascotaClient;
        this.veterinarioClient = veterinarioClient;
        this.kafkaTemplate = kafkaTemplate;
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

        Cita cita = map(new Cita(), request);
        Cita saved = repository.save(cita);

        String evento = "{\"citaId\":" + saved.getId()
                + ",\"mascotaId\":" + saved.getMascotaId() + "}";

        kafkaTemplate.send("cita-creada", evento);

        log.info("Cita creada id={} veterinario={} fecha={} hora={}",
                saved.getId(),
                saved.getVeterinarioId(),
                saved.getFecha(),
                saved.getHora());

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

    private void validarExternos(CitaRequest request) {
        try {
            mascotaClient.findById(request.mascotaId());
            veterinarioClient.findById(request.veterinarioId());
        } catch (FeignException ex) {
            throw new BusinessException("Mascota o veterinario no existe/no disponible");
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
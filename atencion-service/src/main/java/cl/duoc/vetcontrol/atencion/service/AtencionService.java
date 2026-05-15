package cl.duoc.vetcontrol.atencion.service;

import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.repository.AtencionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtencionService {

    private final AtencionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AtencionService(AtencionRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Atencion> findAll() {
        return repository.findAll();
    }

    public Atencion findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención no encontrada: " + id));
    }

    public Atencion create(AtencionRequest request) {
        Atencion saved = repository.save(map(new Atencion(), request));

        String evento = "{\"atencionId\":" + saved.getId()
                + ",\"mascotaId\":" + saved.getMascotaId() + "}";

        kafkaTemplate.send("atencion-registrada", evento);

        return saved;
    }

    public List<Atencion> byMascota(Long mascotaId) {
        return repository.findByMascotaId(mascotaId);
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
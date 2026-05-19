package cl.duoc.vetcontrol.historial.service;

import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.repository.HistorialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialService {

    private final HistorialRepository repository;

    public HistorialService(HistorialRepository repository) {
        this.repository = repository;
    }

    public List<HistorialClinico> all() {
        return repository.findAll();
    }

    public HistorialClinico one(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial no encontrado: " + id));
    }

    public HistorialClinico create(HistorialRequest r) {
        HistorialClinico h = new HistorialClinico();
        h.setMascotaId(r.mascotaId());
        h.setFecha(r.fecha());
        h.setTipo(r.tipo());
        h.setDetalle(r.detalle());
        h.setReferenciaExternaId(r.referenciaExternaId());
        return repository.save(h);
    }

    public List<HistorialClinico> byMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderByFechaDesc(mascotaId);
    }

    // CORREGIDO: usado por el listener Kafka para persistir el evento de atención
    public void registrarDesdeAtencion(Long mascotaId, Long atencionId) {
        HistorialClinico h = new HistorialClinico();
        h.setMascotaId(mascotaId);
        h.setFecha(LocalDateTime.now());
        h.setTipo("ATENCION");
        h.setDetalle("Atención registrada automáticamente desde evento Kafka");
        h.setReferenciaExternaId(atencionId);
        repository.save(h);
    }
}

package cl.duoc.vetcontrol.agenda.controller;

import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cita>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> findByFecha(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return ResponseEntity.ok(
                service.byFecha(fecha)
        );
    }

    @GetMapping("/mascotas/{mascotaId}")
    public ResponseEntity<List<Cita>> findByMascota(
            @PathVariable Long mascotaId
    ) {
        return ResponseEntity.ok(
                service.byMascota(mascotaId)
        );
    }

    @GetMapping("/veterinarios/{veterinarioId}")
    public ResponseEntity<List<Cita>> findByVeterinario(
            @PathVariable Long veterinarioId
    ) {
        return ResponseEntity.ok(
                service.byVeterinario(veterinarioId)
        );
    }

    @PostMapping
    public ResponseEntity<Cita> create(
            @Valid @RequestBody CitaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request
    ) {
        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
package cl.duoc.vetcontrol.atencion.controller;

import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atenciones")
public class AtencionController {

    private final AtencionService service;

    public AtencionController(AtencionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Atencion>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atencion> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/mascotas/{mascotaId}")
    public ResponseEntity<List<Atencion>> findByMascota(
            @PathVariable Long mascotaId
    ) {
        return ResponseEntity.ok(
                service.byMascota(mascotaId)
        );
    }

    @GetMapping("/veterinarios/{veterinarioId}")
    public ResponseEntity<List<Atencion>> findByVeterinario(
            @PathVariable Long veterinarioId
    ) {
        return ResponseEntity.ok(
                service.byVeterinario(veterinarioId)
        );
    }

    @PostMapping
    public ResponseEntity<Atencion> create(
            @Valid @RequestBody AtencionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Atencion> update(
            @PathVariable Long id,
            @Valid @RequestBody AtencionRequest request
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
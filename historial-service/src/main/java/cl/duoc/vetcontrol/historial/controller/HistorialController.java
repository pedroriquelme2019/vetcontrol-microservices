package cl.duoc.vetcontrol.historial.controller;

import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historiales")
public class HistorialController {

    private final HistorialService service;

    public HistorialController(
            HistorialService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HistorialClinico>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialClinico> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/mascotas/{mascotaId}")
    public ResponseEntity<List<HistorialClinico>> findByMascota(
            @PathVariable Long mascotaId
    ) {
        return ResponseEntity.ok(
                service.findByMascota(
                        mascotaId
                )
        );
    }

    @PostMapping
    public ResponseEntity<HistorialClinico> create(
            @Valid @RequestBody HistorialRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }
}
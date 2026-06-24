package cl.duoc.vetcontrol.notificacion.controller;

import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(
            NotificacionService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> findNoLeidas() {
        return ResponseEntity.ok(
                service.findNoLeidas()
        );
    }

    @GetMapping("/tipos/{tipo}")
    public ResponseEntity<List<Notificacion>> findByTipo(
            @PathVariable String tipo
    ) {
        return ResponseEntity.ok(
                service.findByTipo(tipo)
        );
    }

    @PostMapping
    public ResponseEntity<Notificacion> create(
            @Valid
            @RequestBody
            NotificacionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarComoLeida(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.marcarComoLeida(id)
        );
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<Integer> marcarTodasComoLeidas() {
        return ResponseEntity.ok(
                service.marcarTodasComoLeidas()
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
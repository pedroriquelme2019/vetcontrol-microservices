package cl.duoc.vetcontrol.mascota.controller;

import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/mascotas")
public class MascotaController {
    private final MascotaService service; public MascotaController(MascotaService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<Mascota>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<Mascota> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }
    @GetMapping("/cliente/{clienteId}") public ResponseEntity<List<Mascota>> findByCliente(@PathVariable Long clienteId) { return ResponseEntity.ok(service.findByCliente(clienteId)); }
    @PostMapping public ResponseEntity<Mascota> create(@Valid @RequestBody MascotaRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<Mascota> update(@PathVariable Long id, @Valid @RequestBody MascotaRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}

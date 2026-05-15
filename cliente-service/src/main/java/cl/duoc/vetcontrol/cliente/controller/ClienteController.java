package cl.duoc.vetcontrol.cliente.controller;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<Cliente>> findAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<Cliente> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }
    @GetMapping("/buscar") public ResponseEntity<List<Cliente>> search(@RequestParam String nombre) { return ResponseEntity.ok(service.search(nombre)); }
    @PostMapping public ResponseEntity<Cliente> create(@Valid @RequestBody ClienteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<Cliente> update(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}

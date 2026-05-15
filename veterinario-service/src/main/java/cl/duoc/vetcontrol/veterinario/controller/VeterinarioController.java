package cl.duoc.vetcontrol.veterinario.controller;
import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.service.VeterinarioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/veterinarios")
public class VeterinarioController { private final VeterinarioService service; public VeterinarioController(VeterinarioService service){this.service=service;} @GetMapping public ResponseEntity<List<Veterinario>> all(){return ResponseEntity.ok(service.findAll());} @GetMapping("/{id}") public ResponseEntity<Veterinario> one(@PathVariable Long id){return ResponseEntity.ok(service.findById(id));} @GetMapping("/especialidad") public ResponseEntity<List<Veterinario>> byEsp(@RequestParam String nombre){return ResponseEntity.ok(service.byEspecialidad(nombre));} @PostMapping public ResponseEntity<Veterinario> create(@Valid @RequestBody VeterinarioRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));} @PutMapping("/{id}") public ResponseEntity<Veterinario> update(@PathVariable Long id,@Valid @RequestBody VeterinarioRequest r){return ResponseEntity.ok(service.update(id,r));} @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}}

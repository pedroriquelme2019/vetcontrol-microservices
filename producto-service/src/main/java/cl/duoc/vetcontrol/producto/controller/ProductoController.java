package cl.duoc.vetcontrol.producto.controller;
import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/productos")
public class ProductoController { private final ProductoService service; public ProductoController(ProductoService service){this.service=service;} @GetMapping public ResponseEntity<List<Producto>> all(){return ResponseEntity.ok(service.findAll());} @GetMapping("/{id}") public ResponseEntity<Producto> one(@PathVariable Long id){return ResponseEntity.ok(service.findById(id));} @GetMapping("/categoria/{categoria}") public ResponseEntity<List<Producto>> byCategoria(@PathVariable String categoria){return ResponseEntity.ok(service.byCategoria(categoria));} @PostMapping public ResponseEntity<Producto> create(@Valid @RequestBody ProductoRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));} @PutMapping("/{id}") public ResponseEntity<Producto> update(@PathVariable Long id,@Valid @RequestBody ProductoRequest r){return ResponseEntity.ok(service.update(id,r));} @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();} }

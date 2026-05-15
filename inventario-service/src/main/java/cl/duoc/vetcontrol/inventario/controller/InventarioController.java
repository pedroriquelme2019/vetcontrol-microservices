package cl.duoc.vetcontrol.inventario.controller;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/inventario")
public class InventarioController { private final InventarioService service; public InventarioController(InventarioService service){this.service=service;} @GetMapping public ResponseEntity<List<InventarioItem>> all(){return ResponseEntity.ok(service.findAll());} @PostMapping public ResponseEntity<InventarioItem> create(@Valid @RequestBody InventarioRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));} @GetMapping("/productos/{productoId}") public ResponseEntity<InventarioItem> byProducto(@PathVariable Long productoId){return ResponseEntity.ok(service.findByProductoId(productoId));} @GetMapping("/productos/{productoId}/validar/{cantidad}") public ResponseEntity<Boolean> validar(@PathVariable Long productoId,@PathVariable Integer cantidad){return ResponseEntity.ok(service.validarStock(productoId,cantidad));} @PutMapping("/productos/{productoId}/descontar/{cantidad}") public ResponseEntity<Void> descontar(@PathVariable Long productoId,@PathVariable Integer cantidad){service.descontarStock(productoId,cantidad);return ResponseEntity.noContent().build();} @GetMapping("/bajo-stock") public ResponseEntity<List<InventarioItem>> bajoStock(){return ResponseEntity.ok(service.bajoStock());} }

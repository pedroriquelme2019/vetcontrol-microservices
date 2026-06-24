package cl.duoc.vetcontrol.inventario.controller;

import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.dto.InventarioUpdateRequest;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final InventarioService service;

    public InventarioController(
            InventarioService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<InventarioItem>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/productos/{productoId}")
    public ResponseEntity<InventarioItem> findByProducto(
            @PathVariable Long productoId
    ) {
        return ResponseEntity.ok(
                service.findByProductoId(productoId)
        );
    }

    @PostMapping
    public ResponseEntity<InventarioItem> create(
            @Valid @RequestBody InventarioRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/productos/{productoId}")
    public ResponseEntity<InventarioItem> update(
            @PathVariable Long productoId,
            @Valid @RequestBody InventarioUpdateRequest request
    ) {
        return ResponseEntity.ok(
                service.update(productoId, request)
        );
    }

    @GetMapping("/productos/{productoId}/validar/{cantidad}")
    public ResponseEntity<Boolean> validarStock(
            @PathVariable Long productoId,
            @PathVariable Integer cantidad
    ) {
        return ResponseEntity.ok(
                service.validarStock(
                        productoId,
                        cantidad
                )
        );
    }

    @PutMapping("/productos/{productoId}/descontar/{cantidad}")
    public ResponseEntity<InventarioItem> descontarStock(
            @PathVariable Long productoId,
            @PathVariable Integer cantidad
    ) {
        return ResponseEntity.ok(
                service.descontarStock(
                        productoId,
                        cantidad
                )
        );
    }

    @PutMapping("/productos/{productoId}/reponer/{cantidad}")
    public ResponseEntity<InventarioItem> reponerStock(
            @PathVariable Long productoId,
            @PathVariable Integer cantidad
    ) {
        return ResponseEntity.ok(
                service.reponerStock(
                        productoId,
                        cantidad
                )
        );
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioItem>> bajoStock() {
        return ResponseEntity.ok(
                service.bajoStock()
        );
    }

    @DeleteMapping("/productos/{productoId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productoId
    ) {
        service.delete(productoId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
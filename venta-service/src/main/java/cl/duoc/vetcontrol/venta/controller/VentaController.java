package cl.duoc.vetcontrol.venta.controller;

import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService service;

    public VentaController(
            VentaService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Venta>> findAll() {
        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/clientes/{clienteId}")
    public ResponseEntity<List<Venta>> findByCliente(
            @PathVariable Long clienteId
    ) {
        return ResponseEntity.ok(
                service.findByCliente(
                        clienteId
                )
        );
    }

    @PostMapping
    public ResponseEntity<Venta> create(
            @Valid @RequestBody VentaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }
}
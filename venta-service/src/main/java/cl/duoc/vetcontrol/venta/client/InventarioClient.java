package cl.duoc.vetcontrol.venta.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="inventario-service") public interface InventarioClient { @GetMapping("/api/v1/inventario/productos/{productoId}/validar/{cantidad}") Boolean validarStock(@PathVariable("productoId") Long productoId, @PathVariable("cantidad") Integer cantidad); @PutMapping("/api/v1/inventario/productos/{productoId}/descontar/{cantidad}") void descontarStock(@PathVariable("productoId") Long productoId, @PathVariable("cantidad") Integer cantidad); }

package cl.duoc.vetcontrol.venta.client;
import cl.duoc.vetcontrol.venta.dto.ProductoDto; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="producto-service") public interface ProductoClient { @GetMapping("/api/v1/productos/{id}") ProductoDto findById(@PathVariable("id") Long id); }

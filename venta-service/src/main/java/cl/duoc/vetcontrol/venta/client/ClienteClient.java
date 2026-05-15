package cl.duoc.vetcontrol.venta.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.Map;
@FeignClient(name="cliente-service") public interface ClienteClient { @GetMapping("/api/v1/clientes/{id}") Map<String,Object> findById(@PathVariable("id") Long id); }

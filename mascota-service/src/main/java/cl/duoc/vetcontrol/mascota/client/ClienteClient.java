package cl.duoc.vetcontrol.mascota.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "cliente-service")
public interface ClienteClient {
    @GetMapping("/api/v1/clientes/{id}")
    Map<String, Object> findById(@PathVariable("id") Long id);
}

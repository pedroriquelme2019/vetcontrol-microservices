package cl.duoc.vetcontrol.venta.client;

import cl.duoc.vetcontrol.venta.config.FeignSecurityConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "cliente-service",
        configuration = FeignSecurityConfig.class
)
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/{id}")
    Map<String, Object> findById(
            @PathVariable("id") Long id
    );
}
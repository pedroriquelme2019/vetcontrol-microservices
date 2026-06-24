package cl.duoc.vetcontrol.inventario.client;

import cl.duoc.vetcontrol.inventario.config.FeignSecurityConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "producto-service",
        configuration = FeignSecurityConfig.class
)
public interface ProductoClient {

    @GetMapping("/api/v1/productos/{id}")
    Map<String, Object> findById(
            @PathVariable("id") Long id
    );
}
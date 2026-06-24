package cl.duoc.vetcontrol.historial.client;

import cl.duoc.vetcontrol.historial.config.FeignSecurityConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "mascota-service",
        configuration = FeignSecurityConfig.class
)
public interface MascotaClient {

    @GetMapping("/api/v1/mascotas/{id}")
    Map<String, Object> findById(
            @PathVariable("id") Long id
    );
}
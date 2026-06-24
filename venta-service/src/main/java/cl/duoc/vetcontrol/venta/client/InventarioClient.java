package cl.duoc.vetcontrol.venta.client;

import cl.duoc.vetcontrol.venta.config.FeignSecurityConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
        name = "inventario-service",
        configuration = FeignSecurityConfig.class
)
public interface InventarioClient {

    @GetMapping(
            "/api/v1/inventario/productos/{productoId}/validar/{cantidad}"
    )
    Boolean validarStock(
            @PathVariable("productoId") Long productoId,
            @PathVariable("cantidad") Integer cantidad
    );

    @PutMapping(
            "/api/v1/inventario/productos/{productoId}/descontar/{cantidad}"
    )
    void descontarStock(
            @PathVariable("productoId") Long productoId,
            @PathVariable("cantidad") Integer cantidad
    );

    @PutMapping(
            "/api/v1/inventario/productos/{productoId}/reponer/{cantidad}"
    )
    void reponerStock(
            @PathVariable("productoId") Long productoId,
            @PathVariable("cantidad") Integer cantidad
    );
}
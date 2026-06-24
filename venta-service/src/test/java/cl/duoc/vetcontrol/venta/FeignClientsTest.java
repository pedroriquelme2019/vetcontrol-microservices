package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.client.ClienteClient;
import cl.duoc.vetcontrol.venta.client.InventarioClient;
import cl.duoc.vetcontrol.venta.client.ProductoClient;
import cl.duoc.vetcontrol.venta.config.FeignSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FeignClientsTest {

    @Test
    void clienteClientDebeEstarConfigurado() {
        verificarFeign(
                ClienteClient.class,
                "cliente-service"
        );
    }

    @Test
    void productoClientDebeEstarConfigurado() {
        verificarFeign(
                ProductoClient.class,
                "producto-service"
        );
    }

    @Test
    void inventarioClientDebeEstarConfigurado() {
        verificarFeign(
                InventarioClient.class,
                "inventario-service"
        );
    }

    @Test
    void rutasDeInventarioDebenSerCorrectas()
            throws Exception {

        Method validar = InventarioClient.class.getMethod(
                "validarStock",
                Long.class,
                Integer.class
        );

        GetMapping getMapping =
                validar.getAnnotation(GetMapping.class);

        assertArrayEquals(
                new String[]{
                        "/api/v1/inventario/productos/{productoId}/validar/{cantidad}"
                },
                getMapping.value()
        );

        Method descontar = InventarioClient.class.getMethod(
                "descontarStock",
                Long.class,
                Integer.class
        );

        PutMapping descontarMapping =
                descontar.getAnnotation(PutMapping.class);

        assertArrayEquals(
                new String[]{
                        "/api/v1/inventario/productos/{productoId}/descontar/{cantidad}"
                },
                descontarMapping.value()
        );

        Method reponer = InventarioClient.class.getMethod(
                "reponerStock",
                Long.class,
                Integer.class
        );

        PutMapping reponerMapping =
                reponer.getAnnotation(PutMapping.class);

        assertArrayEquals(
                new String[]{
                        "/api/v1/inventario/productos/{productoId}/reponer/{cantidad}"
                },
                reponerMapping.value()
        );
    }

    private void verificarFeign(
            Class<?> cliente,
            String nombre
    ) {
        FeignClient annotation =
                cliente.getAnnotation(FeignClient.class);

        assertNotNull(annotation);
        assertEquals(nombre, annotation.name());

        assertTrue(
                Arrays.asList(annotation.configuration())
                        .contains(FeignSecurityConfig.class)
        );
    }
}
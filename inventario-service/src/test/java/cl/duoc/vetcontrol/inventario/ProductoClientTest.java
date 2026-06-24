package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.client.ProductoClient;
import cl.duoc.vetcontrol.inventario.config.FeignSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProductoClientTest {

    @Test
    void clienteFeignDebeTenerContratoCorrecto()
            throws Exception {

        FeignClient feignClient =
                ProductoClient.class
                        .getAnnotation(
                                FeignClient.class
                        );

        assertNotNull(feignClient);
        assertEquals(
                "producto-service",
                feignClient.name()
        );

        assertTrue(
                Arrays.asList(
                        feignClient.configuration()
                ).contains(
                        FeignSecurityConfig.class
                )
        );

        Method metodo =
                ProductoClient.class.getMethod(
                        "findById",
                        Long.class
                );

        GetMapping mapping =
                metodo.getAnnotation(
                        GetMapping.class
                );

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/api/v1/productos/{id}"
                },
                mapping.value()
        );

        Parameter parametro =
                metodo.getParameters()[0];

        PathVariable pathVariable =
                parametro.getAnnotation(
                        PathVariable.class
                );

        assertNotNull(pathVariable);
        assertEquals(
                "id",
                pathVariable.value()
        );
    }
}
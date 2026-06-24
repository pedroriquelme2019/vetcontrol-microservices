package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.client.MascotaClient;
import cl.duoc.vetcontrol.historial.config.FeignSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MascotaClientTest {

    @Test
    void clienteFeignDebeTenerContratoCorrecto()
            throws Exception {

        FeignClient feignClient =
                MascotaClient.class
                        .getAnnotation(
                                FeignClient.class
                        );

        assertNotNull(feignClient);

        assertEquals(
                "mascota-service",
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
                MascotaClient.class.getMethod(
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
                        "/api/v1/mascotas/{id}"
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
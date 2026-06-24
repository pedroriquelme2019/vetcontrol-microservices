package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import cl.duoc.vetcontrol.mascota.config.FeignSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ClienteClientTest {

    @Test
    void clienteClientDebeTenerConfiguracionFeignCorrecta()
            throws Exception {

        FeignClient feignClient =
                ClienteClient.class
                        .getAnnotation(FeignClient.class);

        assertNotNull(feignClient);
        assertEquals(
                "cliente-service",
                feignClient.name()
        );

        assertTrue(
                Arrays.asList(feignClient.configuration())
                        .contains(FeignSecurityConfig.class)
        );

        Method metodo =
                ClienteClient.class.getMethod(
                        "findById",
                        Long.class
                );

        GetMapping getMapping =
                metodo.getAnnotation(GetMapping.class);

        assertNotNull(getMapping);

        assertArrayEquals(
                new String[]{
                        "/api/v1/clientes/{id}"
                },
                getMapping.value()
        );

        Parameter parametro =
                metodo.getParameters()[0];

        PathVariable pathVariable =
                parametro.getAnnotation(
                        PathVariable.class
                );

        assertNotNull(pathVariable);
        assertEquals("id", pathVariable.value());
    }
}
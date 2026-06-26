package cl.duoc.vetcontrol.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GatewayConfigurationTest {

    @Test
    void applicationYmlDebeTenerConfiguracionPrincipal() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "8080",
                        properties.getProperty(
                                "server.port"
                        )
                ),
                () -> assertEquals(
                        "api-gateway",
                        properties.getProperty(
                                "spring.application.name"
                        )
                ),
                () -> assertEquals(
                        "reactive",
                        properties.getProperty(
                                "spring.main.web-application-type"
                        )
                ),
                () -> assertEquals(
                        "false",
                        properties.getProperty(
                                "spring.cloud.gateway.discovery.locator.enabled"
                        )
                )
        );
    }

    @Test
    void debeTenerTodasLasRutasDelSistema() {
        Properties properties =
                cargarPropiedades();

        List<String> rutasEsperadas =
                List.of(
                        "auth-service",
                        "cliente-service",
                        "mascota-service",
                        "veterinario-service",
                        "agenda-service",
                        "atencion-service",
                        "historial-service",
                        "producto-service",
                        "inventario-service",
                        "venta-service",
                        "notificacion-service"
                );

        List<String> rutasConfiguradas =
                IntStream.range(
                                0,
                                rutasEsperadas.size()
                        )
                        .mapToObj(indice ->
                                properties.getProperty(
                                        "spring.cloud.gateway.routes["
                                                + indice
                                                + "].id"
                                )
                        )
                        .toList();

        assertEquals(
                rutasEsperadas,
                rutasConfiguradas
        );
    }

    @Test
    void cadaRutaDebeUsarLoadBalancer() {
        Properties properties =
                cargarPropiedades();

        for (int indice = 0; indice < 11; indice++) {
            String uri =
                    properties.getProperty(
                            "spring.cloud.gateway.routes["
                                    + indice
                                    + "].uri"
                    );

            assertNotNull(uri);

            assertTrue(
                    uri.startsWith("lb://"),
                    "La ruta " + indice
                            + " no usa lb://"
            );
        }
    }

    @Test
    void rutaAuthDebeIncluirAuthYUsuarios() {
        Properties properties =
                cargarPropiedades();

        assertEquals(
                "Path=/api/v1/auth/**,/api/v1/users/**",
                properties.getProperty(
                        "spring.cloud.gateway.routes[0].predicates[0]"
                )
        );
    }

    @Test
    void rutasFinalesDebenSerCorrectas() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "lb://venta-service",
                        properties.getProperty(
                                "spring.cloud.gateway.routes[9].uri"
                        )
                ),
                () -> assertEquals(
                        "Path=/api/v1/ventas/**",
                        properties.getProperty(
                                "spring.cloud.gateway.routes[9].predicates[0]"
                        )
                ),
                () -> assertEquals(
                        "lb://notificacion-service",
                        properties.getProperty(
                                "spring.cloud.gateway.routes[10].uri"
                        )
                ),
                () -> assertEquals(
                        "Path=/api/v1/notificaciones/**",
                        properties.getProperty(
                                "spring.cloud.gateway.routes[10].predicates[0]"
                        )
                )
        );
    }

    @Test
    void eurekaDebeEstarHabilitado() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "true",
                        properties.getProperty(
                                "eureka.client.register-with-eureka"
                        )
                ),
                () -> assertEquals(
                        "true",
                        properties.getProperty(
                                "eureka.client.fetch-registry"
                        )
                ),
                () -> assertEquals(
                        "http://localhost:8761/eureka/",
                        properties.getProperty(
                                "eureka.client.service-url.defaultZone"
                        )
                )
        );
    }

    private Properties cargarPropiedades() {
        YamlPropertiesFactoryBean factory =
                new YamlPropertiesFactoryBean();

        factory.setResources(
                new ClassPathResource(
                        "application.yml"
                )
        );

        Properties properties =
                factory.getObject();

        assertNotNull(properties);

        return properties;
    }
}
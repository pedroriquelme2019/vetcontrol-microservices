package cl.duoc.vetcontrol.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EurekaConfigurationTest {

    @Test
    void debeConfigurarPuertoYNombreCorrectamente() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "8761",
                        properties.getProperty(
                                "server.port"
                        )
                ),
                () -> assertEquals(
                        "eureka-server",
                        properties.getProperty(
                                "spring.application.name"
                        )
                ),
                () -> assertEquals(
                        "localhost",
                        properties.getProperty(
                                "eureka.instance.hostname"
                        )
                )
        );
    }

    @Test
    void servidorNoDebeRegistrarseConsigoMismo() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "false",
                        properties.getProperty(
                                "eureka.client.register-with-eureka"
                        )
                ),
                () -> assertEquals(
                        "false",
                        properties.getProperty(
                                "eureka.client.fetch-registry"
                        )
                )
        );
    }

    @Test
    void debeTenerUrlEurekaCorrecta() {
        Properties properties =
                cargarPropiedades();

        assertEquals(
                "http://localhost:8761/eureka/",
                properties.getProperty(
                        "eureka.client.service-url.defaultZone"
                )
        );
    }

    @Test
    void selfPreservationDebeEstarDeshabilitadoLocalmente() {
        Properties properties =
                cargarPropiedades();

        assertEquals(
                "false",
                properties.getProperty(
                        "eureka.server.enable-self-preservation"
                )
        );
    }

    @Test
    void actuatorDebeExponerHealthEInfo() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "health,info",
                        properties.getProperty(
                                "management.endpoints.web.exposure.include"
                        )
                ),
                () -> assertEquals(
                        "always",
                        properties.getProperty(
                                "management.endpoint.health.show-details"
                        )
                )
        );
    }

    @Test
    void debeContenerInformacionDeLaAplicacion() {
        Properties properties =
                cargarPropiedades();

        assertAll(
                () -> assertEquals(
                        "Eureka Server VetControl",
                        properties.getProperty(
                                "info.app.name"
                        )
                ),
                () -> assertEquals(
                        "Servidor de descubrimiento de microservicios",
                        properties.getProperty(
                                "info.app.description"
                        )
                ),
                () -> assertEquals(
                        "1.0.0",
                        properties.getProperty(
                                "info.app.version"
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

        assertNotNull(
                properties,
                "No fue posible cargar application.yml"
        );

        return properties;
    }
}
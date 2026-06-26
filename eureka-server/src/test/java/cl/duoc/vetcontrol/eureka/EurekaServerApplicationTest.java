package cl.duoc.vetcontrol.eureka;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class EurekaServerApplicationTest {

    @Test
    void debeTenerAnotacionSpringBootApplication() {
        assertTrue(
                EurekaServerApplication.class
                        .isAnnotationPresent(
                                SpringBootApplication.class
                        )
        );
    }

    @Test
    void debeTenerAnotacionEnableEurekaServer() {
        assertTrue(
                EurekaServerApplication.class
                        .isAnnotationPresent(
                                EnableEurekaServer.class
                        )
        );
    }

    @Test
    void mainDebeIniciarSpringApplication() {
        String[] argumentos = {
                "--spring.main.web-application-type=none"
        };

        ConfigurableApplicationContext contexto =
                mock(
                        ConfigurableApplicationContext.class
                );

        try (
                MockedStatic<SpringApplication> springApplication =
                        mockStatic(
                                SpringApplication.class
                        )
        ) {
            springApplication
                    .when(() ->
                            SpringApplication.run(
                                    EurekaServerApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(contexto);

            EurekaServerApplication.main(
                    argumentos
            );

            springApplication.verify(() ->
                    SpringApplication.run(
                            EurekaServerApplication.class,
                            argumentos
                    )
            );
        }
    }
}
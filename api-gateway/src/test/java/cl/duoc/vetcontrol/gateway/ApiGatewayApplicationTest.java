package cl.duoc.vetcontrol.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ApiGatewayApplicationTest {

    @Test
    void clasePrincipalDebeTenerSpringBootApplication() {
        assertTrue(
                ApiGatewayApplication.class
                        .isAnnotationPresent(
                                SpringBootApplication.class
                        )
        );
    }

    @Test
    void mainDebeIniciarSpringApplication() {
        String[] argumentos = {
                "--spring.main.web-application-type=none"
        };

        ConfigurableApplicationContext context =
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
                                    ApiGatewayApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            ApiGatewayApplication.main(
                    argumentos
            );

            springApplication.verify(() ->
                    SpringApplication.run(
                            ApiGatewayApplication.class,
                            argumentos
                    )
            );
        }
    }
}
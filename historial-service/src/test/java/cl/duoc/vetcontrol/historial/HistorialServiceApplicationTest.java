package cl.duoc.vetcontrol.historial;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HistorialServiceApplicationTest {

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
                                    HistorialServiceApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            HistorialServiceApplication.main(
                    argumentos
            );

            springApplication.verify(() ->
                    SpringApplication.run(
                            HistorialServiceApplication.class,
                            argumentos
                    )
            );
        }
    }

    @Test
    void aplicacionDebeHabilitarFeign() {
        assertTrue(
                HistorialServiceApplication.class
                        .isAnnotationPresent(
                                EnableFeignClients.class
                        )
        );
    }
}
package cl.duoc.vetcontrol.inventario;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class InventarioServiceApplicationTest {

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
                                    InventarioServiceApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            InventarioServiceApplication.main(
                    argumentos
            );

            springApplication.verify(() ->
                    SpringApplication.run(
                            InventarioServiceApplication.class,
                            argumentos
                    )
            );
        }
    }

    @Test
    void aplicacionDebeHabilitarFeignClients() {

        assertTrue(
                InventarioServiceApplication.class
                        .isAnnotationPresent(
                                EnableFeignClients.class
                        )
        );
    }
}
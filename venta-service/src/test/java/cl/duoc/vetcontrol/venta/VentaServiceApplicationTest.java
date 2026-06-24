package cl.duoc.vetcontrol.venta;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class VentaServiceApplicationTest {

    @Test
    void mainDebeIniciarSpringApplication() {
        String[] argumentos = {
                "--spring.main.web-application-type=none"
        };

        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        try (
                MockedStatic<SpringApplication> springApplication =
                        mockStatic(SpringApplication.class)
        ) {
            springApplication
                    .when(() ->
                            SpringApplication.run(
                                    VentaServiceApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            VentaServiceApplication.main(argumentos);

            springApplication.verify(() ->
                    SpringApplication.run(
                            VentaServiceApplication.class,
                            argumentos
                    )
            );
        }
    }

    @Test
    void debeHabilitarClientesFeign() {
        assertTrue(
                VentaServiceApplication.class
                        .isAnnotationPresent(
                                EnableFeignClients.class
                        )
        );
    }
}
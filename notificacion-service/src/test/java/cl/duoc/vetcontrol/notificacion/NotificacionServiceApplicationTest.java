package cl.duoc.vetcontrol.notificacion;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.*;

class NotificacionServiceApplicationTest {

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
                                    NotificacionServiceApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            NotificacionServiceApplication.main(
                    argumentos
            );

            springApplication.verify(() ->
                    SpringApplication.run(
                            NotificacionServiceApplication.class,
                            argumentos
                    )
            );
        }
    }
}
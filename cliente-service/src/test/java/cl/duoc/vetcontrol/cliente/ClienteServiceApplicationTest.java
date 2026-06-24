package cl.duoc.vetcontrol.cliente;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.*;

class ClienteServiceApplicationTest {

    @Test
    void mainDebeIniciarSpringApplication() {

        String[] argumentos =
                new String[]{
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
                                    ClienteServiceApplication.class,
                                    argumentos
                            )
                    )
                    .thenReturn(context);

            ClienteServiceApplication.main(argumentos);

            springApplication.verify(() ->
                    SpringApplication.run(
                            ClienteServiceApplication.class,
                            argumentos
                    )
            );
        }
    }
}
package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorDebeGuardarTodosLosDatos() {
        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        25,
                        20,
                        0
                );

        ErrorResponse response =
                new ErrorResponse(
                        fecha,
                        401,
                        "Unauthorized",
                        "Credenciales inválidas",
                        "/api/v1/auth/login",
                        List.of()
                );

        assertAll(
                () -> assertEquals(
                        fecha,
                        response.getTimestamp()
                ),
                () -> assertEquals(
                        401,
                        response.getStatus()
                ),
                () -> assertEquals(
                        "Unauthorized",
                        response.getError()
                ),
                () -> assertEquals(
                        "Credenciales inválidas",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/auth/login",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of(),
                        response.getDetails()
                )
        );
    }

    @Test
    void settersDebenModificarDatos() {
        ErrorResponse response =
                new ErrorResponse();

        LocalDateTime fecha =
                LocalDateTime.now();

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage(
                "Usuario no encontrado"
        );
        response.setPath(
                "/api/v1/users/99"
        );
        response.setDetails(
                List.of("detalle")
        );

        assertAll(
                () -> assertEquals(
                        fecha,
                        response.getTimestamp()
                ),
                () -> assertEquals(
                        404,
                        response.getStatus()
                ),
                () -> assertEquals(
                        "Not Found",
                        response.getError()
                ),
                () -> assertEquals(
                        "Usuario no encontrado",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/users/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }
}
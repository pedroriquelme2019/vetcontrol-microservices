package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorCompletoDebeGuardarValores() {

        LocalDateTime fecha =
                LocalDateTime.of(2026, 6, 23, 12, 0);

        List<String> detalles =
                List.of("correo: formato inválido");

        ErrorResponse response =
                new ErrorResponse(
                        fecha,
                        400,
                        "Bad Request",
                        "Error de validación",
                        "/api/v1/veterinarios",
                        detalles
                );

        assertAll(
                () -> assertEquals(
                        fecha,
                        response.getTimestamp()
                ),
                () -> assertEquals(
                        400,
                        response.getStatus()
                ),
                () -> assertEquals(
                        "Bad Request",
                        response.getError()
                ),
                () -> assertEquals(
                        "Error de validación",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/veterinarios",
                        response.getPath()
                ),
                () -> assertEquals(
                        detalles,
                        response.getDetails()
                )
        );
    }

    @Test
    void settersDebenModificarValores() {

        ErrorResponse response =
                new ErrorResponse();

        LocalDateTime fecha =
                LocalDateTime.of(2026, 6, 23, 13, 0);

        List<String> detalles =
                List.of("detalle uno", "detalle dos");

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Veterinario no encontrado");
        response.setPath("/api/v1/veterinarios/99");
        response.setDetails(detalles);

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
                        "Veterinario no encontrado",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/veterinarios/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        2,
                        response.getDetails().size()
                )
        );
    }
}
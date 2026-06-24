package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorCompletoDebeGuardarDatos() {
        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        24,
                        20,
                        0
                );

        ErrorResponse response =
                new ErrorResponse(
                        fecha,
                        400,
                        "Bad Request",
                        "Referencia duplicada",
                        "/api/v1/historiales",
                        List.of("detalle")
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
                        "Referencia duplicada",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/historiales",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }

    @Test
    void settersDebenModificarCampos() {
        ErrorResponse response =
                new ErrorResponse();

        LocalDateTime fecha =
                LocalDateTime.now();

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage(
                "Historial no encontrado"
        );
        response.setPath(
                "/api/v1/historiales/99"
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
                        "Historial no encontrado",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/historiales/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }
}
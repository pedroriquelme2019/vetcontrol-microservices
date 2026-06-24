package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorCompletoDebeGuardarDatos() {
        LocalDateTime fecha =
                LocalDateTime.of(2026, 6, 24, 20, 0);

        ErrorResponse response = new ErrorResponse(
                fecha,
                400,
                "Bad Request",
                "Error de validación",
                "/api/v1/citas",
                List.of("motivo: obligatorio")
        );

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(400, response.getStatus()),
                () -> assertEquals("Bad Request", response.getError()),
                () -> assertEquals(
                        "Error de validación",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/citas",
                        response.getPath()
                ),
                () -> assertEquals(
                        1,
                        response.getDetails().size()
                )
        );
    }

    @Test
    void settersDebenModificarDatos() {
        ErrorResponse response = new ErrorResponse();
        LocalDateTime fecha = LocalDateTime.now();

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Cita no encontrada");
        response.setPath("/api/v1/citas/99");
        response.setDetails(List.of("detalle"));

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(404, response.getStatus()),
                () -> assertEquals("Not Found", response.getError()),
                () -> assertEquals(
                        "Cita no encontrada",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/citas/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }
}
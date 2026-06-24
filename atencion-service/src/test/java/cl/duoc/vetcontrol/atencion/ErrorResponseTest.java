package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.exception.ErrorResponse;
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

        List<String> detalles =
                List.of("diagnostico: no debe estar vacío");

        ErrorResponse response = new ErrorResponse(
                fecha,
                400,
                "Bad Request",
                "Error de validación",
                "/api/v1/atenciones",
                detalles
        );

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(400, response.getStatus()),
                () -> assertEquals(
                        "Bad Request",
                        response.getError()
                ),
                () -> assertEquals(
                        "Error de validación",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/atenciones",
                        response.getPath()
                ),
                () -> assertEquals(
                        detalles,
                        response.getDetails()
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
        response.setMessage("Atención no encontrada");
        response.setPath("/api/v1/atenciones/99");
        response.setDetails(List.of("detalle"));

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(404, response.getStatus()),
                () -> assertEquals(
                        "Not Found",
                        response.getError()
                ),
                () -> assertEquals(
                        "Atención no encontrada",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/atenciones/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        1,
                        response.getDetails().size()
                )
        );
    }
}
package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorCompletoDebeGuardarTodosLosDatos() {

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        24,
                        10,
                        30
                );

        List<String> detalles = List.of(
                "precio: debe ser mayor que cero",
                "nombre: no debe estar vacío"
        );

        ErrorResponse response = new ErrorResponse(
                fecha,
                400,
                "Bad Request",
                "Error de validación",
                "/api/v1/productos",
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
                        "/api/v1/productos",
                        response.getPath()
                ),
                () -> assertEquals(
                        detalles,
                        response.getDetails()
                )
        );
    }

    @Test
    void settersDebenModificarTodosLosDatos() {

        ErrorResponse response = new ErrorResponse();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        24,
                        11,
                        0
                );

        List<String> detalles =
                List.of("Detalle de prueba");

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Producto no encontrado");
        response.setPath("/api/v1/productos/99");
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
                        "Producto no encontrado",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/productos/99",
                        response.getPath()
                ),
                () -> assertEquals(
                        detalles,
                        response.getDetails()
                )
        );
    }
}
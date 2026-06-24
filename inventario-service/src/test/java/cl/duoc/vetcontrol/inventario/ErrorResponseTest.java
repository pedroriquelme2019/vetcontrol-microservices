package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.exception.ErrorResponse;
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
                        "Stock insuficiente",
                        "/api/v1/inventario",
                        List.of("detalle")
                );

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(400, response.getStatus()),
                () -> assertEquals("Bad Request", response.getError()),
                () -> assertEquals(
                        "Stock insuficiente",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/inventario",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }

    @Test
    void settersDebenModificarTodosLosCampos() {

        ErrorResponse response =
                new ErrorResponse();

        LocalDateTime fecha =
                LocalDateTime.now();

        response.setTimestamp(fecha);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage(
                "Inventario no encontrado"
        );
        response.setPath(
                "/api/v1/inventario/productos/999"
        );
        response.setDetails(
                List.of("detalle")
        );

        assertAll(
                () -> assertEquals(fecha, response.getTimestamp()),
                () -> assertEquals(404, response.getStatus()),
                () -> assertEquals("Not Found", response.getError()),
                () -> assertEquals(
                        "Inventario no encontrado",
                        response.getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/inventario/productos/999",
                        response.getPath()
                ),
                () -> assertEquals(
                        List.of("detalle"),
                        response.getDetails()
                )
        );
    }
}
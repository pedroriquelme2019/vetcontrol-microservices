package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        ErrorResponse error = new ErrorResponse();

        LocalDateTime now = LocalDateTime.now();

        error.setTimestamp(now);
        error.setStatus(400);
        error.setError("BAD_REQUEST");
        error.setMessage("Error");
        error.setPath("/clientes");
        error.setDetails(List.of("detalle"));

        assertEquals(now, error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("BAD_REQUEST", error.getError());
        assertEquals("Error", error.getMessage());
        assertEquals("/clientes", error.getPath());
        assertEquals(1, error.getDetails().size());
    }

    @Test
    void constructorCompletoDebeFuncionar() {

        LocalDateTime now = LocalDateTime.now();

        ErrorResponse error =
                new ErrorResponse(
                        now,
                        404,
                        "NOT_FOUND",
                        "No existe",
                        "/clientes/1",
                        List.of("detalle")
                );

        assertEquals(404, error.getStatus());
        assertEquals("NOT_FOUND", error.getError());
        assertEquals("No existe", error.getMessage());
    }
}
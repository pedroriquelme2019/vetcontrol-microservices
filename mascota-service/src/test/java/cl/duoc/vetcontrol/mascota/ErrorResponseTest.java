package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void gettersYSettersFuncionan() {

        ErrorResponse error =
                new ErrorResponse();

        LocalDateTime now =
                LocalDateTime.now();

        error.setTimestamp(now);
        error.setStatus(400);
        error.setError("BAD_REQUEST");
        error.setMessage("Error");
        error.setPath("/api");
        error.setDetails(List.of("detalle"));

        assertEquals(now,error.getTimestamp());
        assertEquals(400,error.getStatus());
        assertEquals("BAD_REQUEST",error.getError());
        assertEquals("Error",error.getMessage());
        assertEquals("/api",error.getPath());
        assertEquals(1,error.getDetails().size());
    }
}
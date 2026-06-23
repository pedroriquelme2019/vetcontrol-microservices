package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleNotFoundDebeRetornar404() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/mascotas/99");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("No existe"),
                        request
                );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("No existe",
                response.getBody().getMessage());
    }

    @Test
    void handleBusinessDebeRetornar400() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/mascotas");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException("Error negocio"),
                        request
                );

        assertEquals(400,
                response.getStatusCode().value());
    }

    @Test
    void handleGeneralDebeRetornar500() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/mascotas");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException("Error"),
                        request
                );

        assertEquals(500,
                response.getStatusCode().value());
    }
}
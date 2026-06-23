package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.exception.*;
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
                .thenReturn("/clientes/1");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("No encontrado"),
                        request
                );

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void handleBusinessDebeRetornar400() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/clientes");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException("Error negocio"),
                        request
                );

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleGeneralDebeRetornar500() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/clientes");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException("Error"),
                        request
                );

        assertEquals(500, response.getStatusCode().value());
    }
}
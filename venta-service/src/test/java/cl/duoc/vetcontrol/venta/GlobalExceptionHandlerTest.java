package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.exception.BusinessException;
import cl.duoc.vetcontrol.venta.exception.ErrorResponse;
import cl.duoc.vetcontrol.venta.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.venta.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/ventas");
    }

    @Test
    void handleNotFoundDebeRetornar404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Venta no encontrada: 99"
                        ),
                        request
                );

        assertEquals(404, response.getStatusCode().value());
        assertEquals(
                "Venta no encontrada: 99",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException(
                                "Stock insuficiente"
                        ),
                        request
                );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Stock insuficiente",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleValidationDebeCrearDetalles() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError(
                                "ventaRequest",
                                "medioPago",
                                "no permitido"
                        )
                ));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(
                        exception,
                        request
                );

        assertEquals(400, response.getStatusCode().value());

        assertEquals(
                List.of("medioPago: no permitido"),
                response.getBody().getDetails()
        );
    }

    @Test
    void handleGeneralDebeRetornarMensajeSeguro() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Error interno MySQL"
                        ),
                        request
                );

        assertEquals(500, response.getStatusCode().value());

        assertEquals(
                "Error interno del servidor",
                response.getBody().getMessage()
        );
    }
}
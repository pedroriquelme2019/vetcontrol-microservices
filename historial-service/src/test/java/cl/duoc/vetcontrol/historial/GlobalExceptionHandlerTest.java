package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.exception.BusinessException;
import cl.duoc.vetcontrol.historial.exception.ErrorResponse;
import cl.duoc.vetcontrol.historial.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
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
        handler =
                new GlobalExceptionHandler();

        request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn(
                        "/api/v1/historiales"
                );
    }

    @Test
    void handleNotFoundDebeRetornar404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Historial no encontrado: 99"
                        ),
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Historial no encontrado: 99",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException(
                                "Referencia duplicada"
                        ),
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Referencia duplicada",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleValidationDebeRetornarDetalles() {
        MethodArgumentNotValidException exception =
                mock(
                        MethodArgumentNotValidException.class
                );

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError(
                                "historialRequest",
                                "tipo",
                                "no permitido"
                        )
                ));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(
                        exception,
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                List.of("tipo: no permitido"),
                response.getBody().getDetails()
        );
    }

    @Test
    void handleGeneralDebeRetornarMensajeSeguro() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Contraseña de MySQL"
                        ),
                        request
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertEquals(
                "Error interno del servidor",
                response.getBody().getMessage()
        );

        assertNotEquals(
                "Contraseña de MySQL",
                response.getBody().getMessage()
        );
    }
}
package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.ErrorResponse;
import cl.duoc.vetcontrol.agenda.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
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
                .thenReturn("/api/v1/citas");
    }

    @Test
    void handleNotFoundDebeRetornar404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Cita no encontrada: 99"
                        ),
                        request
                );

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        "Not Found",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Cita no encontrada: 99",
                        response.getBody().getMessage()
                )
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException(
                                "Horario ocupado"
                        ),
                        request
                );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Horario ocupado",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleValidationDebeRetornarDetalles() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError(
                                "citaRequest",
                                "motivo",
                                "no debe estar vacío"
                        )
                ));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(
                        exception,
                        request
                );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                List.of("motivo: no debe estar vacío"),
                response.getBody().getDetails()
        );
    }

    @Test
    void handleGeneralDebeRetornar500Seguro() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Contraseña MySQL"
                        ),
                        request
                );

        assertEquals(500, response.getStatusCode().value());
        assertEquals(
                "Error interno del servidor",
                response.getBody().getMessage()
        );

        assertNotEquals(
                "Contraseña MySQL",
                response.getBody().getMessage()
        );
    }
}
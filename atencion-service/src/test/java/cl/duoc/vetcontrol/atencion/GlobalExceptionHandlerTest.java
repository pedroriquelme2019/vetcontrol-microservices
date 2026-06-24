package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.exception.BusinessException;
import cl.duoc.vetcontrol.atencion.exception.ErrorResponse;
import cl.duoc.vetcontrol.atencion.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
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
                .thenReturn("/api/v1/atenciones");
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Atención no encontrada: 99"
                        ),
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        "Not Found",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Atención no encontrada: 99",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/atenciones",
                        response.getBody().getPath()
                ),
                () -> assertTrue(
                        response.getBody()
                                .getDetails()
                                .isEmpty()
                )
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException(
                                "Cita duplicada"
                        ),
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Cita duplicada",
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
                                "atencionRequest",
                                "diagnostico",
                                "no debe estar vacío"
                        ),
                        new FieldError(
                                "atencionRequest",
                                "tratamiento",
                                "no debe estar vacío"
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

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        "Error de validación",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        2,
                        response.getBody()
                                .getDetails()
                                .size()
                ),
                () -> assertTrue(
                        response.getBody()
                                .getDetails()
                                .contains(
                                        "diagnostico: no debe estar vacío"
                                )
                )
        );
    }

    @Test
    void handleGeneralDebeRetornar500Seguro() {

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Contraseña MySQL expuesta"
                        ),
                        request
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        "Internal Server Error",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Error interno del servidor",
                        response.getBody().getMessage()
                ),
                () -> assertNotEquals(
                        "Contraseña MySQL expuesta",
                        response.getBody().getMessage()
                )
        );
    }
}
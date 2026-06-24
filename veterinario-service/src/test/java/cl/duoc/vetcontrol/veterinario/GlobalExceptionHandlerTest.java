package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.exception.BusinessException;
import cl.duoc.vetcontrol.veterinario.exception.ErrorResponse;
import cl.duoc.vetcontrol.veterinario.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.veterinario.exception.ResourceNotFoundException;
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
                .thenReturn("/api/v1/veterinarios");
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Veterinario no encontrado: 99"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(exception, request);

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        404,
                        response.getBody().getStatus()
                ),
                () -> assertEquals(
                        "Not Found",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Veterinario no encontrado: 99",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/veterinarios",
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

        BusinessException exception =
                new BusinessException("RUT duplicado");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(exception, request);

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "RUT duplicado",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleValidationDebeRetornar400YDetalles() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError(
                                "veterinarioRequest",
                                "correo",
                                "debe ser una dirección de correo válida"
                        ),
                        new FieldError(
                                "veterinarioRequest",
                                "nombre",
                                "no debe estar vacío"
                        )
                ));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(exception, request);

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
                                        "correo: debe ser una dirección de correo válida"
                                )
                ),
                () -> assertTrue(
                        response.getBody()
                                .getDetails()
                                .contains(
                                        "nombre: no debe estar vacío"
                                )
                )
        );
    }

    @Test
    void handleGeneralDebeRetornar500SinExponerErrorInterno() {

        Exception exception =
                new RuntimeException(
                        "Información sensible de la base de datos"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(exception, request);

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        "Error interno del servidor",
                        response.getBody().getMessage()
                ),
                () -> assertNotEquals(
                        exception.getMessage(),
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/veterinarios",
                        response.getBody().getPath()
                )
        );
    }
}
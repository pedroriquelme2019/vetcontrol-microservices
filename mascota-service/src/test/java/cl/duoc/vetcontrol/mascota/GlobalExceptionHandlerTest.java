package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import cl.duoc.vetcontrol.mascota.exception.ErrorResponse;
import cl.duoc.vetcontrol.mascota.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
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
                .thenReturn("/api/v1/mascotas");
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Mascota no encontrada: 99"
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
                        "Mascota no encontrada: 99",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/mascotas",
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
                                "Cliente dueño inválido"
                        ),
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Cliente dueño inválido",
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
                                "mascotaRequest",
                                "nombre",
                                "no debe estar vacío"
                        ),
                        new FieldError(
                                "mascotaRequest",
                                "peso",
                                "debe ser positivo o cero"
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
                                        "nombre: no debe estar vacío"
                                )
                )
        );
    }

    @Test
    void handleGeneralDebeRetornar500Seguro() {

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Error interno de MySQL"
                        ),
                        request
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Error interno del servidor",
                response.getBody().getMessage()
        );

        assertNotEquals(
                "Error interno de MySQL",
                response.getBody().getMessage()
        );
    }
}
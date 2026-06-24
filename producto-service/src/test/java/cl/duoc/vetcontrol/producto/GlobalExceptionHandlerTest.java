package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.exception.BusinessException;
import cl.duoc.vetcontrol.producto.exception.ErrorResponse;
import cl.duoc.vetcontrol.producto.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
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
                .thenReturn("/api/v1/productos");
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Producto no encontrado: 99"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertNotNull(
                        response.getBody().getTimestamp()
                ),
                () -> assertEquals(
                        404,
                        response.getBody().getStatus()
                ),
                () -> assertEquals(
                        "Not Found",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Producto no encontrado: 99",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/productos",
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
                new BusinessException(
                        "Operación no permitida"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
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
                        "Bad Request",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Operación no permitida",
                        response.getBody().getMessage()
                ),
                () -> assertTrue(
                        response.getBody()
                                .getDetails()
                                .isEmpty()
                )
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
                                "productoRequest",
                                "nombre",
                                "no debe estar vacío"
                        ),
                        new FieldError(
                                "productoRequest",
                                "precio",
                                "debe ser mayor que cero"
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
                ),
                () -> assertTrue(
                        response.getBody()
                                .getDetails()
                                .contains(
                                        "precio: debe ser mayor que cero"
                                )
                )
        );
    }

    @Test
    void handleGeneralDebeRetornar500SinExponerErrorReal() {

        RuntimeException exception =
                new RuntimeException(
                        "Error sensible de MySQL"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        exception,
                        request
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertAll(
                () -> assertEquals(
                        500,
                        response.getBody().getStatus()
                ),
                () -> assertEquals(
                        "Internal Server Error",
                        response.getBody().getError()
                ),
                () -> assertEquals(
                        "Error interno del servidor",
                        response.getBody().getMessage()
                ),
                () -> assertNotEquals(
                        exception.getMessage(),
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/productos",
                        response.getBody().getPath()
                )
        );
    }
}
package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.exception.BusinessException;
import cl.duoc.vetcontrol.inventario.exception.ErrorResponse;
import cl.duoc.vetcontrol.inventario.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.inventario.exception.ResourceNotFoundException;
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
                        "/api/v1/inventario"
                );
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Inventario no encontrado"
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
                        "Inventario no encontrado",
                        response.getBody().getMessage()
                ),
                () -> assertEquals(
                        "/api/v1/inventario",
                        response.getBody().getPath()
                )
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

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Stock insuficiente",
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
                                "inventarioRequest",
                                "stockActual",
                                "debe ser mayor o igual que 0"
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
                "Error de validación",
                response.getBody().getMessage()
        );

        assertEquals(
                List.of(
                        "stockActual: debe ser mayor o igual que 0"
                ),
                response.getBody().getDetails()
        );
    }

    @Test
    void handleGeneralDebeOcultarDetalleInterno() {

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
package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.exception.ErrorResponse;
import cl.duoc.vetcontrol.auth.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.auth.exception.InvalidCredentialsException;
import cl.duoc.vetcontrol.auth.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
                .thenReturn("/api/v1/users");
    }

    @Test
    void handleNotFoundDebeRetornar404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "Usuario no encontrado: 99"
                        ),
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertEquals(
                "Usuario no encontrado: 99",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleInvalidCredentialsDebeRetornar401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(
                        new InvalidCredentialsException(
                                "Credenciales inválidas"
                        ),
                        request
                );

        assertEquals(
                401,
                response.getStatusCode().value()
        );

        assertEquals(
                "Credenciales inválidas",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException(
                                "El username ya existe"
                        ),
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );
    }

    @Test
    void handleConflictDebeRetornar409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(
                        new DataIntegrityViolationException(
                                "Duplicado"
                        ),
                        request
                );

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        assertEquals(
                "El username o correo ya está registrado",
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
                                "userRequest",
                                "email",
                                "correo inválido"
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
                List.of(
                        "email: correo inválido"
                ),
                response.getBody().getDetails()
        );
    }

    @Test
    void handleUnreadableMessageDebeRetornar400() {
        HttpMessageNotReadableException exception =
                mock(
                        HttpMessageNotReadableException.class
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUnreadableMessage(
                        exception,
                        request
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Cuerpo de solicitud inválido",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleGeneralDebeRetornar500Seguro() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "Error interno MySQL"
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
    }
}
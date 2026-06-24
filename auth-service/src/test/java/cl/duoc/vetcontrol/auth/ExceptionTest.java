package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.exception.InvalidCredentialsException;
import cl.duoc.vetcontrol.auth.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionTest {

    @Test
    void businessExceptionDebeConservarMensaje() {
        BusinessException exception =
                new BusinessException(
                        "El username ya existe"
                );

        assertEquals(
                "El username ya existe",
                exception.getMessage()
        );
    }

    @Test
    void invalidCredentialsDebeConservarMensaje() {
        InvalidCredentialsException exception =
                new InvalidCredentialsException(
                        "Credenciales inválidas"
                );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );
    }

    @Test
    void resourceNotFoundDebeConservarMensaje() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Usuario no encontrado: 99"
                );

        assertEquals(
                "Usuario no encontrado: 99",
                exception.getMessage()
        );
    }
}
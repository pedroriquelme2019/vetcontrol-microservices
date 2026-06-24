package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.dto.UserUpdateRequest;
import cl.duoc.vetcontrol.auth.model.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserUpdateRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void todosLosCamposNulosDebenSerValidos() {
        UserUpdateRequest request =
                new UserUpdateRequest(
                        null,
                        null,
                        null,
                        null
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void requestCompletoDebeSerValido() {
        UserUpdateRequest request =
                new UserUpdateRequest(
                        "nuevo@vetcontrol.cl",
                        "password123",
                        Role.RECEPCIONISTA,
                        false
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void correoInvalidoDebeSerRechazado() {
        UserUpdateRequest request =
                new UserUpdateRequest(
                        "correo-invalido",
                        null,
                        null,
                        null
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "email"
                )
        );
    }

    @Test
    void passwordCortaDebeSerRechazada() {
        UserUpdateRequest request =
                new UserUpdateRequest(
                        null,
                        "123",
                        null,
                        null
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "password"
                )
        );
    }

    private boolean tieneError(
            Set<? extends ConstraintViolation<?>> errores,
            String campo
    ) {
        return errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }
}
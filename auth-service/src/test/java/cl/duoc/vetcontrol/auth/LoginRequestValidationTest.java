package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestValidationTest {

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
    void requestValidoNoDebeTenerErrores() {
        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "admin123"
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void usernameVacioDebeSerInvalido() {
        LoginRequest request =
                new LoginRequest(
                        "",
                        "admin123"
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "username"
                )
        );
    }

    @Test
    void passwordVaciaDebeSerInvalida() {
        LoginRequest request =
                new LoginRequest(
                        "admin",
                        " "
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "password"
                )
        );
    }

    @Test
    void usernameSuperiorA120DebeSerInvalido() {
        LoginRequest request =
                new LoginRequest(
                        "a".repeat(121),
                        "admin123"
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "username"
                )
        );
    }

    @Test
    void passwordSuperiorA72DebeSerInvalida() {
        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "a".repeat(73)
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
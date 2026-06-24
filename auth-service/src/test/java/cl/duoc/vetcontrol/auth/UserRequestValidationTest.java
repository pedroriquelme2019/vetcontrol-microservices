package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.dto.UserRequest;
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

class UserRequestValidationTest {

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
        UserRequest request = new UserRequest(
                "nuevo.usuario",
                "nuevo@vetcontrol.cl",
                "password123",
                Role.VETERINARIO
        );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void usernameCortoDebeSerInvalido() {
        UserRequest request = crearRequest("ab");

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "username"
                )
        );
    }

    @Test
    void usernameConEspaciosDebeSerInvalido() {
        UserRequest request =
                crearRequest("usuario nuevo");

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "username"
                )
        );
    }

    @Test
    void correoInvalidoDebeSerRechazado() {
        UserRequest request = new UserRequest(
                "usuario",
                "correo-invalido",
                "password123",
                Role.ADMIN
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "email"
                )
        );
    }

    @Test
    void passwordCortaDebeSerInvalida() {
        UserRequest request = new UserRequest(
                "usuario",
                "usuario@vetcontrol.cl",
                "1234567",
                Role.ADMIN
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "password"
                )
        );
    }

    @Test
    void rolNuloDebeSerInvalido() {
        UserRequest request = new UserRequest(
                "usuario",
                "usuario@vetcontrol.cl",
                "password123",
                null
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "role"
                )
        );
    }

    private UserRequest crearRequest(
            String username
    ) {
        return new UserRequest(
                username,
                "usuario@vetcontrol.cl",
                "password123",
                Role.ADMIN
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
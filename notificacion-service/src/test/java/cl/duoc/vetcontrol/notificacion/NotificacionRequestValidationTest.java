package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory =
                Validation
                        .buildDefaultValidatorFactory();

        validator =
                factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void requestValidoNoDebeTenerErrores() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "Mensaje válido"
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );

        assertEquals(
                TipoNotificacion.MANUAL,
                request.tipo()
        );

        assertEquals(
                "Mensaje válido",
                request.mensaje()
        );
    }

    @Test
    void tipoNuloDebeSerInvalido() {
        NotificacionRequest request =
                new NotificacionRequest(
                        null,
                        "Mensaje válido"
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "tipo"
                )
        );
    }

    @Test
    void mensajeNuloDebeSerInvalido() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        null
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "mensaje"
                )
        );
    }

    @Test
    void mensajeVacioDebeSerInvalido() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "   "
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "mensaje"
                )
        );
    }

    @Test
    void mensajeSuperiorA500CaracteresDebeSerInvalido() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "a".repeat(501)
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "mensaje"
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
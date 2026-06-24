package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HistorialRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory =
                Validation.buildDefaultValidatorFactory();

        validator =
                factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void requestValidoNoDebeTenerErrores() {
        HistorialRequest request =
                crearRequestValido();

        assertTrue(
                validator.validate(request).isEmpty()
        );

        assertAll(
                () -> assertEquals(
                        10L,
                        request.mascotaId()
                ),
                () -> assertEquals(
                        "VACUNA",
                        request.tipo()
                ),
                () -> assertEquals(
                        50L,
                        request.referenciaExternaId()
                )
        );
    }

    @Test
    void requestInvalidoDebeDetectarTodosLosCampos() {
        HistorialRequest request =
                new HistorialRequest(
                        null,
                        LocalDateTime.now().plusDays(1),
                        "",
                        "",
                        -1L
                );

        Set<ConstraintViolation<HistorialRequest>> errores =
                validator.validate(request);

        assertTrue(
                tieneError(
                        errores,
                        "mascotaId"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "fecha"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "tipo"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "detalle"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "referenciaExternaId"
                )
        );
    }

    @Test
    void tipoMinusculaDebeSerValido() {
        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "vacuna",
                        "Detalle",
                        null
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void tipoNoPermitidoDebeSerInvalido() {
        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "OTRO",
                        "Detalle",
                        null
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "tipo"
                )
        );
    }

    @Test
    void detalleSuperiorA500CaracteresDebeSerInvalido() {
        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "OBSERVACION",
                        "a".repeat(501),
                        null
                );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "detalle"
                )
        );
    }

    @Test
    void referenciaNulaDebeSerValida() {
        HistorialRequest request =
                new HistorialRequest(
                        10L,
                        LocalDateTime.now().minusHours(1),
                        "OBSERVACION",
                        "Paciente estable",
                        null
                );

        assertTrue(
                validator.validate(request).isEmpty()
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

    private HistorialRequest crearRequestValido() {
        return new HistorialRequest(
                10L,
                LocalDateTime.now().minusHours(1),
                "VACUNA",
                "Vacuna aplicada",
                50L
        );
    }
}
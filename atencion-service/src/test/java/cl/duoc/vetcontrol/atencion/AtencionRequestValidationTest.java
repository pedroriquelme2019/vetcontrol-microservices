package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
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

class AtencionRequestValidationTest {

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

        AtencionRequest request = crearRequestValido();

        Set<ConstraintViolation<AtencionRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());

        assertAll(
                () -> assertEquals(5L, request.citaId()),
                () -> assertEquals(10L, request.mascotaId()),
                () -> assertEquals(20L, request.veterinarioId()),
                () -> assertEquals(
                        "Dermatitis",
                        request.diagnostico()
                ),
                () -> assertEquals(
                        "Antihistamínico",
                        request.tratamiento()
                )
        );
    }

    @Test
    void datosInvalidosDebenGenerarSieteErrores() {

        AtencionRequest request =
                new AtencionRequest(
                        null,
                        0L,
                        -1L,
                        LocalDateTime.now().plusDays(1),
                        "",
                        "",
                        "a".repeat(501)
                );

        Set<ConstraintViolation<AtencionRequest>> errores =
                validator.validate(request);

        assertEquals(7, errores.size());

        assertTrue(tieneError(errores, "citaId"));
        assertTrue(tieneError(errores, "mascotaId"));
        assertTrue(tieneError(errores, "veterinarioId"));
        assertTrue(tieneError(errores, "fechaAtencion"));
        assertTrue(tieneError(errores, "diagnostico"));
        assertTrue(tieneError(errores, "tratamiento"));
        assertTrue(tieneError(errores, "observaciones"));
    }

    @Test
    void observacionesPuedenSerNulas() {

        AtencionRequest request =
                new AtencionRequest(
                        1L,
                        2L,
                        3L,
                        LocalDateTime.now().minusMinutes(5),
                        "Diagnóstico",
                        "Tratamiento",
                        null
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void diagnosticoYTratamientoNoPuedenSuperarLimite() {

        AtencionRequest request =
                new AtencionRequest(
                        1L,
                        2L,
                        3L,
                        LocalDateTime.now(),
                        "a".repeat(301),
                        "b".repeat(301),
                        null
                );

        Set<ConstraintViolation<AtencionRequest>> errores =
                validator.validate(request);

        assertEquals(2, errores.size());
        assertTrue(tieneError(errores, "diagnostico"));
        assertTrue(tieneError(errores, "tratamiento"));
    }

    private boolean tieneError(
            Set<ConstraintViolation<AtencionRequest>> errores,
            String campo
    ) {
        return errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }

    private AtencionRequest crearRequestValido() {
        return new AtencionRequest(
                5L,
                10L,
                20L,
                LocalDateTime.now().minusHours(1),
                "Dermatitis",
                "Antihistamínico",
                "Control en siete días"
        );
    }
}
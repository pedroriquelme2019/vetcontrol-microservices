package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CitaRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void requestValidoNoDebeTenerErrores() {
        CitaRequest request = crearRequestValido();

        assertTrue(
                validator.validate(request).isEmpty()
        );

        assertAll(
                () -> assertEquals(10L, request.mascotaId()),
                () -> assertEquals(20L, request.veterinarioId()),
                () -> assertEquals("Control", request.motivo())
        );
    }

    @Test
    void datosInvalidosDebenGenerarCincoErrores() {
        CitaRequest request = new CitaRequest(
                null,
                0L,
                LocalDate.now().minusDays(1),
                null,
                ""
        );

        Set<ConstraintViolation<CitaRequest>> errores =
                validator.validate(request);

        assertEquals(5, errores.size());

        assertTrue(tieneError(errores, "mascotaId"));
        assertTrue(tieneError(errores, "veterinarioId"));
        assertTrue(tieneError(errores, "fecha"));
        assertTrue(tieneError(errores, "hora"));
        assertTrue(tieneError(errores, "motivo"));
    }

    @Test
    void motivoNoPuedeSuperar160Caracteres() {
        CitaRequest request = new CitaRequest(
                10L,
                20L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "a".repeat(161)
        );

        Set<ConstraintViolation<CitaRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "motivo"));
    }

    @Test
    void fechaActualEsValidaParaElDto() {
        CitaRequest request = new CitaRequest(
                10L,
                20L,
                LocalDate.now(),
                LocalTime.MAX,
                "Control"
        );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    private boolean tieneError(
            Set<ConstraintViolation<CitaRequest>> errores,
            String campo
    ) {
        return errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }

    private CitaRequest crearRequestValido() {
        return new CitaRequest(
                10L,
                20L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Control"
        );
    }
}
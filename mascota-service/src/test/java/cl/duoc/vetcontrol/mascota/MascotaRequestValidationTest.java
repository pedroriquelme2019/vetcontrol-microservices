package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MascotaRequestValidationTest {

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

        MascotaRequest request = crearRequestValido();

        Set<ConstraintViolation<MascotaRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void camposObligatoriosYNumerosInvalidosDebenFallar() {

        MascotaRequest request = new MascotaRequest(
                null,
                "",
                "",
                null,
                -1,
                null,
                -0.1,
                null
        );

        Set<ConstraintViolation<MascotaRequest>> errores =
                validator.validate(request);

        assertEquals(5, errores.size());

        assertTrue(tieneError(errores, "clienteId"));
        assertTrue(tieneError(errores, "nombre"));
        assertTrue(tieneError(errores, "especie"));
        assertTrue(tieneError(errores, "edad"));
        assertTrue(tieneError(errores, "peso"));
    }

    @Test
    void edadYCeroPesoDebenSerValidos() {

        MascotaRequest request = new MascotaRequest(
                1L,
                "Cachorro",
                "Perro",
                null,
                0,
                null,
                0.0,
                null
        );

        Set<ConstraintViolation<MascotaRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void camposOpcionalesPuedenSerNulos() {

        MascotaRequest request = new MascotaRequest(
                1L,
                "Luna",
                "Gato",
                null,
                null,
                null,
                null,
                null
        );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    private boolean tieneError(
            Set<ConstraintViolation<MascotaRequest>> errores,
            String campo
    ) {
        return errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }

    private MascotaRequest crearRequestValido() {
        return new MascotaRequest(
                1L,
                "Firulais",
                "Perro",
                "Labrador",
                5,
                "Macho",
                18.5,
                "CHIP-001"
        );
    }
}
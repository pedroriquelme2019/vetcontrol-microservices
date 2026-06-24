package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VeterinarioRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    static void cerrarValidator() {

        validatorFactory.close();
    }

    @Test
    void requestValidoNoDebeTenerErrores() {

        VeterinarioRequest request =
                new VeterinarioRequest(
                        "11111111-1",
                        "Juan Pérez",
                        "Cirugía",
                        "juan@correo.cl"
                );

        Set<ConstraintViolation<VeterinarioRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void requestVacioDebeTenerCuatroErrores() {

        VeterinarioRequest request =
                new VeterinarioRequest(
                        "",
                        "",
                        "",
                        ""
                );

        Set<ConstraintViolation<VeterinarioRequest>> errores =
                validator.validate(request);

        assertEquals(4, errores.size());

        assertTrue(errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("rut")));

        assertTrue(errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("nombre")));

        assertTrue(errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("especialidad")));

        assertTrue(errores.stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("correo")));
    }

    @Test
    void correoConFormatoInvalidoDebeGenerarError() {

        VeterinarioRequest request =
                new VeterinarioRequest(
                        "11111111-1",
                        "Juan Pérez",
                        "Cirugía",
                        "correo-invalido"
                );

        Set<ConstraintViolation<VeterinarioRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertEquals(
                "correo",
                errores.iterator()
                        .next()
                        .getPropertyPath()
                        .toString()
        );
    }
}
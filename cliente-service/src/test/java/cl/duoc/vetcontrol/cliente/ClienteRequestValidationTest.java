package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClienteRequestValidationTest {

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

        ClienteRequest request =
                new ClienteRequest(
                        "11111111-1",
                        "Joaquín González",
                        "+56999999999",
                        "joaquin@correo.cl",
                        "Recoleta"
                );

        Set<ConstraintViolation<ClienteRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void camposVaciosDebenGenerarCincoErrores() {

        ClienteRequest request =
                new ClienteRequest(
                        "",
                        "",
                        "",
                        "",
                        ""
                );

        Set<ConstraintViolation<ClienteRequest>> errores =
                validator.validate(request);

        assertEquals(5, errores.size());
    }

    @Test
    void correoInvalidoDebeGenerarError() {

        ClienteRequest request =
                new ClienteRequest(
                        "11111111-1",
                        "Joaquín",
                        "+56999999999",
                        "correo-invalido",
                        "Recoleta"
                );

        Set<ConstraintViolation<ClienteRequest>> errores =
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

    @Test
    void rutMayorADoceCaracteresDebeGenerarError() {

        ClienteRequest request =
                new ClienteRequest(
                        "1234567890123",
                        "Joaquín",
                        "+56999999999",
                        "joaquin@correo.cl",
                        "Recoleta"
                );

        Set<ConstraintViolation<ClienteRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());

        assertEquals(
                "rut",
                errores.iterator()
                        .next()
                        .getPropertyPath()
                        .toString()
        );
    }
}
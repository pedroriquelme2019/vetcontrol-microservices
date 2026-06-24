package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.dto.InventarioUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InventarioRequestValidationTest {

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
    void inventarioRequestValidoNoDebeTenerErrores() {

        InventarioRequest request =
                new InventarioRequest(
                        100L,
                        10,
                        3
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );

        assertAll(
                () -> assertEquals(100L, request.productoId()),
                () -> assertEquals(10, request.stockActual()),
                () -> assertEquals(3, request.stockMinimo())
        );
    }

    @Test
    void inventarioRequestInvalidoDebeGenerarTresErrores() {

        InventarioRequest request =
                new InventarioRequest(
                        null,
                        -1,
                        -1
                );

        Set<ConstraintViolation<InventarioRequest>> errores =
                validator.validate(request);

        assertEquals(3, errores.size());

        assertTrue(
                tieneError(
                        errores,
                        "productoId"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "stockActual"
                )
        );

        assertTrue(
                tieneError(
                        errores,
                        "stockMinimo"
                )
        );
    }

    @Test
    void productoIdCeroDebeSerInvalido() {

        InventarioRequest request =
                new InventarioRequest(
                        0L,
                        0,
                        0
                );

        Set<ConstraintViolation<InventarioRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(
                tieneError(
                        errores,
                        "productoId"
                )
        );
    }

    @Test
    void stockEnCeroDebeSerValido() {

        InventarioRequest request =
                new InventarioRequest(
                        100L,
                        0,
                        0
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void updateRequestValidoNoDebeTenerErrores() {

        InventarioUpdateRequest request =
                new InventarioUpdateRequest(
                        20,
                        5
                );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void updateRequestInvalidoDebeGenerarDosErrores() {

        InventarioUpdateRequest request =
                new InventarioUpdateRequest(
                        -1,
                        -1
                );

        Set<ConstraintViolation<InventarioUpdateRequest>> errores =
                validator.validate(request);

        assertEquals(2, errores.size());
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
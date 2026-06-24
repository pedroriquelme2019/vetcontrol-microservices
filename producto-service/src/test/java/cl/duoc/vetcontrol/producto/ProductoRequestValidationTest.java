package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductoRequestValidationTest {

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
    void requestValidoNoDebeGenerarErrores() {

        ProductoRequest request = new ProductoRequest(
                "Vacuna antirrábica",
                "Medicamento",
                new BigDecimal("15000.00"),
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());

        assertAll(
                () -> assertEquals(
                        "Vacuna antirrábica",
                        request.nombre()
                ),
                () -> assertEquals(
                        "Medicamento",
                        request.categoria()
                ),
                () -> assertEquals(
                        new BigDecimal("15000.00"),
                        request.precio()
                ),
                () -> assertFalse(request.restringido())
        );
    }

    @Test
    void nombreVacioDebeGenerarError() {

        ProductoRequest request = new ProductoRequest(
                "",
                "Medicamento",
                new BigDecimal("10000.00"),
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "nombre"));
    }

    @Test
    void categoriaVaciaDebeGenerarError() {

        ProductoRequest request = new ProductoRequest(
                "Vacuna",
                "",
                new BigDecimal("10000.00"),
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "categoria"));
    }

    @Test
    void precioNuloDebeGenerarError() {

        ProductoRequest request = new ProductoRequest(
                "Vacuna",
                "Medicamento",
                null,
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "precio"));
    }

    @Test
    void precioCeroDebeGenerarError() {

        ProductoRequest request = new ProductoRequest(
                "Vacuna",
                "Medicamento",
                BigDecimal.ZERO,
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "precio"));
    }

    @Test
    void precioNegativoDebeGenerarError() {

        ProductoRequest request = new ProductoRequest(
                "Vacuna",
                "Medicamento",
                new BigDecimal("-1000.00"),
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(1, errores.size());
        assertTrue(tieneError(errores, "precio"));
    }

    @Test
    void datosCompletamenteInvalidosDebenGenerarTresErrores() {

        ProductoRequest request = new ProductoRequest(
                "",
                "",
                null,
                false
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertEquals(3, errores.size());
        assertTrue(tieneError(errores, "nombre"));
        assertTrue(tieneError(errores, "categoria"));
        assertTrue(tieneError(errores, "precio"));
    }

    @Test
    void productoRestringidoTambiénPuedeSerValido() {

        ProductoRequest request = new ProductoRequest(
                "Anestesia veterinaria",
                "Medicamento controlado",
                new BigDecimal("50000.00"),
                true
        );

        Set<ConstraintViolation<ProductoRequest>> errores =
                validator.validate(request);

        assertTrue(errores.isEmpty());
        assertTrue(request.restringido());
    }

    private boolean tieneError(
            Set<ConstraintViolation<ProductoRequest>> errores,
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
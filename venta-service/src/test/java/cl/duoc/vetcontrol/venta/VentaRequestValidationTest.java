package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VentaRequestValidationTest {

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
    void ventaRequestValidoNoDebeTenerErrores() {
        VentaRequest request = crearRequestValido();

        assertTrue(
                validator.validate(request).isEmpty()
        );

        assertAll(
                () -> assertEquals(10L, request.clienteId()),
                () -> assertEquals("EFECTIVO", request.medioPago()),
                () -> assertEquals(1, request.detalles().size())
        );
    }

    @Test
    void clienteCeroDebeSerInvalido() {
        VentaRequest request = new VentaRequest(
                0L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(100L, 1)
                )
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "clienteId"
                )
        );
    }

    @Test
    void medioPagoNoPermitidoDebeSerInvalido() {
        VentaRequest request = new VentaRequest(
                10L,
                "CHEQUE",
                List.of(
                        new DetalleVentaRequest(100L, 1)
                )
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "medioPago"
                )
        );
    }

    @Test
    void medioPagoMinusculaDebeSerValido() {
        VentaRequest request = new VentaRequest(
                10L,
                "credito",
                List.of(
                        new DetalleVentaRequest(100L, 1)
                )
        );

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void detallesVaciosDebenSerInvalidos() {
        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                List.of()
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "detalles"
                )
        );
    }

    @Test
    void noDebeAceptarMasDeCincuentaDetalles() {
        List<DetalleVentaRequest> detalles =
                new ArrayList<>();

        for (int i = 1; i <= 51; i++) {
            detalles.add(
                    new DetalleVentaRequest(
                            (long) i,
                            1
                    )
            );
        }

        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                detalles
        );

        assertTrue(
                tieneError(
                        validator.validate(request),
                        "detalles"
                )
        );
    }

    @Test
    void detalleConProductoYCantidadInvalidosDebeFallar() {
        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(
                                0L,
                                0
                        )
                )
        );

        Set<ConstraintViolation<VentaRequest>> errores =
                validator.validate(request);

        assertFalse(errores.isEmpty());

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .contains("productoId")
                )
        );

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .contains("cantidad")
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

    private VentaRequest crearRequestValido() {
        return new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(
                                100L,
                                2
                        )
                )
        );
    }
}
package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.dto.ProductoDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductoDtoTest {

    @Test
    void recordDebeExponerTodosLosDatos() {
        ProductoDto producto = new ProductoDto(
                100L,
                "Antiparasitario",
                "MEDICAMENTO",
                new BigDecimal("5990.00"),
                true,
                true
        );

        assertAll(
                () -> assertEquals(100L, producto.id()),
                () -> assertEquals(
                        "Antiparasitario",
                        producto.nombre()
                ),
                () -> assertEquals(
                        "MEDICAMENTO",
                        producto.categoria()
                ),
                () -> assertEquals(
                        new BigDecimal("5990.00"),
                        producto.precio()
                ),
                () -> assertTrue(producto.restringido()),
                () -> assertTrue(producto.activo())
        );
    }
}
package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductoModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        Producto producto = new Producto();

        producto.setId(1L);
        producto.setNombre("Antiparasitario");
        producto.setCategoria("Medicamento");
        producto.setPrecio(new BigDecimal("15000.00"));
        producto.setRestringido(true);
        producto.setActivo(false);

        assertAll(
                () -> assertEquals(
                        1L,
                        producto.getId()
                ),
                () -> assertEquals(
                        "Antiparasitario",
                        producto.getNombre()
                ),
                () -> assertEquals(
                        "Medicamento",
                        producto.getCategoria()
                ),
                () -> assertEquals(
                        new BigDecimal("15000.00"),
                        producto.getPrecio()
                ),
                () -> assertTrue(
                        producto.isRestringido()
                ),
                () -> assertFalse(
                        producto.isActivo()
                )
        );
    }

    @Test
    void productoNuevoDebeTenerValoresPredeterminados() {

        Producto producto = new Producto();

        assertAll(
                () -> assertNull(producto.getId()),
                () -> assertNull(producto.getNombre()),
                () -> assertNull(producto.getCategoria()),
                () -> assertNull(producto.getPrecio()),
                () -> assertFalse(producto.isRestringido()),
                () -> assertTrue(producto.isActivo())
        );
    }
}
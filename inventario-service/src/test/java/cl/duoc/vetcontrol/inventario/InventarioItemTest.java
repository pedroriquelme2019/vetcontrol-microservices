package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventarioItemTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        InventarioItem item =
                new InventarioItem();

        item.setId(1L);
        item.setProductoId(100L);
        item.setStockActual(10);
        item.setStockMinimo(3);
        item.setActivo(false);

        assertAll(
                () -> assertEquals(1L, item.getId()),
                () -> assertEquals(100L, item.getProductoId()),
                () -> assertEquals(10, item.getStockActual()),
                () -> assertEquals(3, item.getStockMinimo()),
                () -> assertFalse(item.isActivo())
        );
    }

    @Test
    void inventarioNuevoDebeEstarActivoPorDefecto() {

        InventarioItem item =
                new InventarioItem();

        assertAll(
                () -> assertNull(item.getId()),
                () -> assertNull(item.getProductoId()),
                () -> assertNull(item.getStockActual()),
                () -> assertNull(item.getStockMinimo()),
                () -> assertTrue(item.isActivo())
        );
    }
}
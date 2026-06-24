package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DetalleVentaModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {
        Venta venta = new Venta();
        DetalleVenta detalle = new DetalleVenta();

        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setProductoId(100L);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(
                new BigDecimal("2500.00")
        );
        detalle.setSubtotal(
                new BigDecimal("5000.00")
        );

        assertAll(
                () -> assertEquals(1L, detalle.getId()),
                () -> assertSame(venta, detalle.getVenta()),
                () -> assertEquals(100L, detalle.getProductoId()),
                () -> assertEquals(2, detalle.getCantidad()),
                () -> assertEquals(
                        new BigDecimal("2500.00"),
                        detalle.getPrecioUnitario()
                ),
                () -> assertEquals(
                        new BigDecimal("5000.00"),
                        detalle.getSubtotal()
                )
        );
    }
}
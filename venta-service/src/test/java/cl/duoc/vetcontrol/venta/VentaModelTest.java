package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VentaModelTest {

    @Test
    void ventaNuevaDebeTenerValoresPredeterminados() {
        Venta venta = new Venta();

        assertAll(
                () -> assertNull(venta.getId()),
                () -> assertNull(venta.getClienteId()),
                () -> assertNotNull(venta.getFecha()),
                () -> assertNull(venta.getMedioPago()),
                () -> assertEquals(BigDecimal.ZERO, venta.getTotal()),
                () -> assertEquals(
                        EstadoVenta.PENDIENTE,
                        venta.getEstado()
                ),
                () -> assertTrue(venta.getDetalles().isEmpty())
        );
    }

    @Test
    void gettersYSettersDebenFuncionar() {
        Venta venta = new Venta();

        LocalDateTime fecha =
                LocalDateTime.of(2026, 7, 10, 12, 0);

        venta.setId(1L);
        venta.setClienteId(10L);
        venta.setFecha(fecha);
        venta.setMedioPago("DEBITO");
        venta.setTotal(new BigDecimal("3000.00"));
        venta.setEstado(EstadoVenta.REGISTRADA);

        assertAll(
                () -> assertEquals(1L, venta.getId()),
                () -> assertEquals(10L, venta.getClienteId()),
                () -> assertEquals(fecha, venta.getFecha()),
                () -> assertEquals("DEBITO", venta.getMedioPago()),
                () -> assertEquals(
                        new BigDecimal("3000.00"),
                        venta.getTotal()
                ),
                () -> assertEquals(
                        EstadoVenta.REGISTRADA,
                        venta.getEstado()
                )
        );
    }

    @Test
    void agregarDetalleDebeEstablecerRelacionBidireccional() {
        Venta venta = new Venta();
        DetalleVenta detalle = crearDetalle(100L);

        venta.agregarDetalle(detalle);

        assertEquals(1, venta.getDetalles().size());
        assertSame(venta, detalle.getVenta());
    }

    @Test
    void setDetallesDebeReemplazarListaYAsignarVenta() {
        Venta venta = new Venta();

        DetalleVenta primero = crearDetalle(100L);
        venta.agregarDetalle(primero);

        DetalleVenta segundo = crearDetalle(200L);

        venta.setDetalles(List.of(segundo));

        assertEquals(1, venta.getDetalles().size());
        assertEquals(
                200L,
                venta.getDetalles().get(0).getProductoId()
        );
        assertSame(venta, segundo.getVenta());
    }

    @Test
    void setDetallesNuloDebeVaciarLista() {
        Venta venta = new Venta();

        venta.agregarDetalle(crearDetalle(100L));
        venta.setDetalles(null);

        assertTrue(venta.getDetalles().isEmpty());
    }

    private DetalleVenta crearDetalle(Long productoId) {
        DetalleVenta detalle = new DetalleVenta();

        detalle.setProductoId(productoId);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("1000.00"));
        detalle.setSubtotal(new BigDecimal("1000.00"));

        return detalle;
    }
}
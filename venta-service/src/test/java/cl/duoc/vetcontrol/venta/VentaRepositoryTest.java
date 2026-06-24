package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class VentaRepositoryTest {

    @Autowired
    private VentaRepository repository;

    @Test
    void saveDebeGuardarVentaConDetallesEnCascada() {
        Venta venta = crearVenta(
                10L,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                new BigDecimal("5000.00")
        );

        DetalleVenta detalle = crearDetalle(
                100L,
                2,
                new BigDecimal("2500.00")
        );

        venta.agregarDetalle(detalle);

        Venta guardada =
                repository.saveAndFlush(venta);

        assertAll(
                () -> assertNotNull(guardada.getId()),
                () -> assertEquals(1, guardada.getDetalles().size()),
                () -> assertNotNull(
                        guardada.getDetalles().get(0).getId()
                ),
                () -> assertSame(
                        guardada,
                        guardada.getDetalles().get(0).getVenta()
                )
        );
    }

    @Test
    void findAllDebeOrdenarPorFechaDescendente() {
        repository.saveAndFlush(
                crearVenta(
                        10L,
                        LocalDateTime.of(2026, 7, 1, 10, 0),
                        new BigDecimal("1000.00")
                )
        );

        repository.saveAndFlush(
                crearVenta(
                        20L,
                        LocalDateTime.of(2026, 7, 2, 10, 0),
                        new BigDecimal("2000.00")
                )
        );

        List<Venta> resultado =
                repository.findAllByOrderByFechaDesc();

        assertEquals(2, resultado.size());
        assertEquals(20L, resultado.get(0).getClienteId());
        assertEquals(10L, resultado.get(1).getClienteId());
    }

    @Test
    void findByClienteDebeFiltrarYOrdenar() {
        repository.saveAndFlush(
                crearVenta(
                        10L,
                        LocalDateTime.of(2026, 7, 1, 10, 0),
                        new BigDecimal("1000.00")
                )
        );

        repository.saveAndFlush(
                crearVenta(
                        10L,
                        LocalDateTime.of(2026, 7, 3, 10, 0),
                        new BigDecimal("3000.00")
                )
        );

        repository.saveAndFlush(
                crearVenta(
                        20L,
                        LocalDateTime.of(2026, 7, 4, 10, 0),
                        new BigDecimal("4000.00")
                )
        );

        List<Venta> resultado =
                repository.findByClienteIdOrderByFechaDesc(10L);

        assertEquals(2, resultado.size());

        assertTrue(
                resultado.get(0).getFecha()
                        .isAfter(resultado.get(1).getFecha())
        );

        assertTrue(
                resultado.stream()
                        .allMatch(venta ->
                                venta.getClienteId().equals(10L)
                        )
        );
    }

    private Venta crearVenta(
            Long clienteId,
            LocalDateTime fecha,
            BigDecimal total
    ) {
        Venta venta = new Venta();

        venta.setClienteId(clienteId);
        venta.setFecha(fecha);
        venta.setMedioPago("EFECTIVO");
        venta.setTotal(total);
        venta.setEstado(EstadoVenta.REGISTRADA);

        return venta;
    }

    private DetalleVenta crearDetalle(
            Long productoId,
            Integer cantidad,
            BigDecimal precio
    ) {
        DetalleVenta detalle = new DetalleVenta();

        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(
                precio.multiply(
                        BigDecimal.valueOf(cantidad)
                )
        );

        return detalle;
    }
}
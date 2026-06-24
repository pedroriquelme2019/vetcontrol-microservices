package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClienteModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        Cliente cliente = new Cliente();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        23,
                        15,
                        30
                );

        cliente.setId(1L);
        cliente.setRut("11111111-1");
        cliente.setNombre("Joaquín González");
        cliente.setTelefono("+56999999999");
        cliente.setCorreo("joaquin@correo.cl");
        cliente.setDireccion("Recoleta");
        cliente.setActivo(false);
        cliente.setCreatedAt(fecha);

        assertAll(
                () -> assertEquals(1L, cliente.getId()),
                () -> assertEquals(
                        "11111111-1",
                        cliente.getRut()
                ),
                () -> assertEquals(
                        "Joaquín González",
                        cliente.getNombre()
                ),
                () -> assertEquals(
                        "+56999999999",
                        cliente.getTelefono()
                ),
                () -> assertEquals(
                        "joaquin@correo.cl",
                        cliente.getCorreo()
                ),
                () -> assertEquals(
                        "Recoleta",
                        cliente.getDireccion()
                ),
                () -> assertFalse(cliente.isActivo()),
                () -> assertEquals(
                        fecha,
                        cliente.getCreatedAt()
                )
        );
    }

    @Test
    void clienteNuevoDebeTenerValoresPorDefecto() {

        Cliente cliente = new Cliente();

        assertTrue(cliente.isActivo());
        assertNotNull(cliente.getCreatedAt());
    }
}
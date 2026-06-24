package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.model.Mascota;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MascotaModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        Mascota mascota = new Mascota();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        23,
                        18,
                        0
                );

        mascota.setId(1L);
        mascota.setClienteId(10L);
        mascota.setNombre("Firulais");
        mascota.setEspecie("Perro");
        mascota.setRaza("Labrador");
        mascota.setEdad(5);
        mascota.setSexo("Macho");
        mascota.setPeso(18.5);
        mascota.setMicrochip("CHIP-001");
        mascota.setActivo(false);
        mascota.setCreatedAt(fecha);

        assertAll(
                () -> assertEquals(1L, mascota.getId()),
                () -> assertEquals(10L, mascota.getClienteId()),
                () -> assertEquals("Firulais", mascota.getNombre()),
                () -> assertEquals("Perro", mascota.getEspecie()),
                () -> assertEquals("Labrador", mascota.getRaza()),
                () -> assertEquals(5, mascota.getEdad()),
                () -> assertEquals("Macho", mascota.getSexo()),
                () -> assertEquals(18.5, mascota.getPeso()),
                () -> assertEquals(
                        "CHIP-001",
                        mascota.getMicrochip()
                ),
                () -> assertFalse(mascota.isActivo()),
                () -> assertEquals(
                        fecha,
                        mascota.getCreatedAt()
                )
        );
    }

    @Test
    void mascotaNuevaDebeTenerValoresPorDefecto() {

        Mascota mascota = new Mascota();

        assertTrue(mascota.isActivo());
        assertNotNull(mascota.getCreatedAt());
    }
}
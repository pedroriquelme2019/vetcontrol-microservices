package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeterinarioModelTest {

    @Test
    void gettersYSettersDebenFuncionar() {

        Veterinario veterinario = new Veterinario();

        veterinario.setId(1L);
        veterinario.setRut("11111111-1");
        veterinario.setNombre("Juan Pérez");
        veterinario.setEspecialidad("Cirugía");
        veterinario.setCorreo("juan@correo.cl");
        veterinario.setActivo(false);

        assertAll(
                () -> assertEquals(1L, veterinario.getId()),
                () -> assertEquals(
                        "11111111-1",
                        veterinario.getRut()
                ),
                () -> assertEquals(
                        "Juan Pérez",
                        veterinario.getNombre()
                ),
                () -> assertEquals(
                        "Cirugía",
                        veterinario.getEspecialidad()
                ),
                () -> assertEquals(
                        "juan@correo.cl",
                        veterinario.getCorreo()
                ),
                () -> assertFalse(veterinario.isActivo())
        );
    }

    @Test
    void veterinarioNuevoDebeEstarActivoPorDefecto() {

        Veterinario veterinario = new Veterinario();

        assertTrue(veterinario.isActivo());
    }
}
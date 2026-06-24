package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.repository.MascotaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class MascotaRepositoryTest {

    @Autowired
    private MascotaRepository repository;

    @Test
    void saveDebeGuardarMascotaCompleta() {

        Mascota guardada = repository.saveAndFlush(
                crearMascota(
                        1L,
                        "Firulais",
                        "Perro",
                        true
                )
        );

        assertAll(
                () -> assertNotNull(guardada.getId()),
                () -> assertEquals(1L, guardada.getClienteId()),
                () -> assertEquals("Firulais", guardada.getNombre()),
                () -> assertEquals("Perro", guardada.getEspecie()),
                () -> assertNotNull(guardada.getCreatedAt()),
                () -> assertTrue(guardada.isActivo())
        );
    }

    @Test
    void findByActivoTrueDebeExcluirInactivas() {

        repository.saveAndFlush(
                crearMascota(1L, "Activa", "Perro", true)
        );

        repository.saveAndFlush(
                crearMascota(1L, "Inactiva", "Gato", false)
        );

        List<Mascota> resultado =
                repository.findByActivoTrue();

        assertEquals(1, resultado.size());
        assertEquals("Activa", resultado.get(0).getNombre());
    }

    @Test
    void findByIdAndActivoTrueDebeEncontrarActiva() {

        Mascota guardada = repository.saveAndFlush(
                crearMascota(1L, "Luna", "Gato", true)
        );

        Optional<Mascota> resultado =
                repository.findByIdAndActivoTrue(
                        guardada.getId()
                );

        assertTrue(resultado.isPresent());
        assertEquals("Luna", resultado.get().getNombre());
    }

    @Test
    void findByIdAndActivoTrueDebeIgnorarInactiva() {

        Mascota guardada = repository.saveAndFlush(
                crearMascota(1L, "Tom", "Gato", false)
        );

        Optional<Mascota> resultado =
                repository.findByIdAndActivoTrue(
                        guardada.getId()
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByClienteIdDebeRetornarSoloActivasDelCliente() {

        repository.saveAndFlush(
                crearMascota(10L, "Max", "Perro", true)
        );

        repository.saveAndFlush(
                crearMascota(10L, "Rocky", "Perro", false)
        );

        repository.saveAndFlush(
                crearMascota(20L, "Michi", "Gato", true)
        );

        List<Mascota> resultado =
                repository.findByClienteIdAndActivoTrue(10L);

        assertEquals(1, resultado.size());
        assertEquals("Max", resultado.get(0).getNombre());
    }

    @Test
    void searchDebeIgnorarMayusculasYExcluirInactivas() {

        repository.saveAndFlush(
                crearMascota(
                        1L,
                        "Firulais",
                        "Perro",
                        true
                )
        );

        repository.saveAndFlush(
                crearMascota(
                        1L,
                        "Firulais inactivo",
                        "Perro",
                        false
                )
        );

        repository.saveAndFlush(
                crearMascota(
                        1L,
                        "Luna",
                        "Gato",
                        true
                )
        );

        List<Mascota> resultado =
                repository
                        .findByNombreContainingIgnoreCaseAndActivoTrue(
                                "FIRU"
                        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Firulais",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void searchDebeRetornarListaVacia() {

        List<Mascota> resultado =
                repository
                        .findByNombreContainingIgnoreCaseAndActivoTrue(
                                "NoExiste"
                        );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    private Mascota crearMascota(
            Long clienteId,
            String nombre,
            String especie,
            boolean activo
    ) {
        Mascota mascota = new Mascota();

        mascota.setClienteId(clienteId);
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza("Sin especificar");
        mascota.setEdad(3);
        mascota.setSexo("Sin especificar");
        mascota.setPeso(10.0);
        mascota.setMicrochip(
                "CHIP-" + clienteId + "-" + nombre
        );
        mascota.setActivo(activo);

        return mascota;
    }
}
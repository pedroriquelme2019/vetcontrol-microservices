package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.repository.VeterinarioRepository;
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
class VeterinarioRepositoryTest {

    @Autowired
    private VeterinarioRepository repository;

    @Test
    void saveDebeGuardarVeterinario() {

        Veterinario veterinario = crearVeterinario(
                "11111111-1",
                "Juan Pérez",
                "Cirugía",
                "juan@correo.cl"
        );

        Veterinario guardado =
                repository.saveAndFlush(veterinario);

        assertAll(
                () -> assertNotNull(guardado.getId()),
                () -> assertEquals(
                        "11111111-1",
                        guardado.getRut()
                ),
                () -> assertEquals(
                        "Juan Pérez",
                        guardado.getNombre()
                ),
                () -> assertEquals(
                        "Cirugía",
                        guardado.getEspecialidad()
                ),
                () -> assertEquals(
                        "juan@correo.cl",
                        guardado.getCorreo()
                ),
                () -> assertTrue(guardado.isActivo())
        );
    }

    @Test
    void findByIdDebeRetornarVeterinarioExistente() {

        Veterinario guardado =
                repository.saveAndFlush(
                        crearVeterinario(
                                "12121212-1",
                                "Laura Gómez",
                                "Medicina general",
                                "laura@correo.cl"
                        )
                );

        Optional<Veterinario> resultado =
                repository.findById(guardado.getId());

        assertTrue(resultado.isPresent());

        assertEquals(
                "Laura Gómez",
                resultado.get().getNombre()
        );
    }

    @Test
    void findAllDebeRetornarVeterinariosGuardados() {

        repository.saveAndFlush(
                crearVeterinario(
                        "13131313-1",
                        "Andrea Ruiz",
                        "Cirugía",
                        "andrea@correo.cl"
                )
        );

        repository.saveAndFlush(
                crearVeterinario(
                        "14141414-1",
                        "Felipe Soto",
                        "Dermatología",
                        "felipe@correo.cl"
                )
        );

        List<Veterinario> resultado =
                repository.findAll();

        assertEquals(2, resultado.size());
    }

    @Test
    void existsByRutDebeRetornarTrueCuandoExiste() {

        repository.saveAndFlush(
                crearVeterinario(
                        "22222222-2",
                        "María González",
                        "Dermatología",
                        "maria@correo.cl"
                )
        );

        boolean existe =
                repository.existsByRut("22222222-2");

        assertTrue(existe);
    }

    @Test
    void existsByRutDebeRetornarFalseCuandoNoExiste() {

        boolean existe =
                repository.existsByRut("99999999-9");

        assertFalse(existe);
    }

    @Test
    void existsByCorreoDebeRetornarTrueCuandoExiste() {

        repository.saveAndFlush(
                crearVeterinario(
                        "33333333-3",
                        "Pedro Soto",
                        "Cardiología",
                        "pedro@correo.cl"
                )
        );

        boolean existe =
                repository.existsByCorreo("pedro@correo.cl");

        assertTrue(existe);
    }

    @Test
    void existsByCorreoDebeRetornarFalseCuandoNoExiste() {

        boolean existe =
                repository.existsByCorreo(
                        "noexiste@correo.cl"
                );

        assertFalse(existe);
    }

    @Test
    void findByEspecialidadDebeIgnorarMayusculasYMinusculas() {

        repository.saveAndFlush(
                crearVeterinario(
                        "44444444-4",
                        "Ana Torres",
                        "Cirugía General",
                        "ana@correo.cl"
                )
        );

        repository.saveAndFlush(
                crearVeterinario(
                        "55555555-5",
                        "Carlos Díaz",
                        "Dermatología",
                        "carlos@correo.cl"
                )
        );

        List<Veterinario> resultado =
                repository
                        .findByEspecialidadContainingIgnoreCase(
                                "CIRUGÍA"
                        );

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(
                        "Ana Torres",
                        resultado.get(0).getNombre()
                ),
                () -> assertEquals(
                        "Cirugía General",
                        resultado.get(0).getEspecialidad()
                )
        );
    }

    @Test
    void findByEspecialidadDebeEncontrarCoincidenciaParcial() {

        repository.saveAndFlush(
                crearVeterinario(
                        "66666666-6",
                        "Sofía Morales",
                        "Medicina Felina",
                        "sofia@correo.cl"
                )
        );

        List<Veterinario> resultado =
                repository
                        .findByEspecialidadContainingIgnoreCase(
                                "felina"
                        );

        assertEquals(1, resultado.size());

        assertEquals(
                "Sofía Morales",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void findByEspecialidadDebeRetornarListaVacia() {

        List<Veterinario> resultado =
                repository
                        .findByEspecialidadContainingIgnoreCase(
                                "Oncología"
                        );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deleteDebeEliminarVeterinarioDeLaBaseDePrueba() {

        Veterinario guardado =
                repository.saveAndFlush(
                        crearVeterinario(
                                "77777777-7",
                                "Tomás Pérez",
                                "Neurología",
                                "tomas@correo.cl"
                        )
                );

        Long id = guardado.getId();

        repository.delete(guardado);
        repository.flush();

        assertFalse(repository.existsById(id));
    }

    private Veterinario crearVeterinario(
            String rut,
            String nombre,
            String especialidad,
            String correo
    ) {
        Veterinario veterinario =
                new Veterinario();

        veterinario.setRut(rut);
        veterinario.setNombre(nombre);
        veterinario.setEspecialidad(especialidad);
        veterinario.setCorreo(correo);
        veterinario.setActivo(true);

        return veterinario;
    }
}
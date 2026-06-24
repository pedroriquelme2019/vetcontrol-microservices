package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.repository.AtencionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
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
class AtencionRepositoryTest {

    @Autowired
    private AtencionRepository repository;

    @Test
    void saveDebeGuardarAtencionCompleta() {

        Atencion guardada = repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, true)
        );

        assertAll(
                () -> assertNotNull(guardada.getId()),
                () -> assertEquals(1L, guardada.getCitaId()),
                () -> assertEquals(10L, guardada.getMascotaId()),
                () -> assertEquals(20L, guardada.getVeterinarioId()),
                () -> assertEquals("Dermatitis", guardada.getDiagnostico()),
                () -> assertEquals("Antihistamínico", guardada.getTratamiento()),
                () -> assertTrue(guardada.isActivo())
        );
    }

    @Test
    void findByActivoTrueDebeExcluirInactivas() {

        repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, true)
        );

        repository.saveAndFlush(
                crearAtencion(2L, 11L, 21L, false)
        );

        List<Atencion> resultado =
                repository.findByActivoTrue();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getCitaId());
    }

    @Test
    void findByIdAndActivoTrueDebeEncontrarActiva() {

        Atencion guardada = repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, true)
        );

        Optional<Atencion> resultado =
                repository.findByIdAndActivoTrue(
                        guardada.getId()
                );

        assertTrue(resultado.isPresent());
        assertEquals(
                guardada.getId(),
                resultado.get().getId()
        );
    }

    @Test
    void findByIdAndActivoTrueDebeIgnorarInactiva() {

        Atencion guardada = repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, false)
        );

        Optional<Atencion> resultado =
                repository.findByIdAndActivoTrue(
                        guardada.getId()
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByMascotaDebeRetornarSoloActivas() {

        repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, true)
        );

        repository.saveAndFlush(
                crearAtencion(2L, 10L, 21L, false)
        );

        repository.saveAndFlush(
                crearAtencion(3L, 30L, 20L, true)
        );

        List<Atencion> resultado =
                repository.findByMascotaIdAndActivoTrue(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getMascotaId());
    }

    @Test
    void findByVeterinarioDebeRetornarSoloActivas() {

        repository.saveAndFlush(
                crearAtencion(1L, 10L, 20L, true)
        );

        repository.saveAndFlush(
                crearAtencion(2L, 11L, 20L, false)
        );

        repository.saveAndFlush(
                crearAtencion(3L, 12L, 30L, true)
        );

        List<Atencion> resultado =
                repository.findByVeterinarioIdAndActivoTrue(20L);

        assertEquals(1, resultado.size());
        assertEquals(
                20L,
                resultado.get(0).getVeterinarioId()
        );
    }

    @Test
    void existsByCitaIdDebeConsiderarSoloActivas() {

        repository.saveAndFlush(
                crearAtencion(5L, 10L, 20L, true)
        );

        repository.saveAndFlush(
                crearAtencion(6L, 11L, 21L, false)
        );

        assertTrue(
                repository.existsByCitaIdAndActivoTrue(5L)
        );

        assertFalse(
                repository.existsByCitaIdAndActivoTrue(6L)
        );
    }

    @Test
    void existsByCitaIdAndIdNotDebeDetectarOtraAtencion() {

        Atencion primera = repository.saveAndFlush(
                crearAtencion(5L, 10L, 20L, true)
        );

        Atencion segunda = repository.saveAndFlush(
                crearAtencion(5L, 11L, 21L, true)
        );

        assertTrue(
                repository
                        .existsByCitaIdAndIdNotAndActivoTrue(
                                5L,
                                primera.getId()
                        )
        );

        segunda.setActivo(false);
        repository.saveAndFlush(segunda);

        assertFalse(
                repository
                        .existsByCitaIdAndIdNotAndActivoTrue(
                                5L,
                                primera.getId()
                        )
        );
    }

    private Atencion crearAtencion(
            Long citaId,
            Long mascotaId,
            Long veterinarioId,
            boolean activo
    ) {
        Atencion atencion = new Atencion();

        atencion.setCitaId(citaId);
        atencion.setMascotaId(mascotaId);
        atencion.setVeterinarioId(veterinarioId);
        atencion.setFechaAtencion(
                LocalDateTime.now().minusHours(1)
        );
        atencion.setDiagnostico("Dermatitis");
        atencion.setTratamiento("Antihistamínico");
        atencion.setObservaciones("Control posterior");
        atencion.setActivo(activo);

        return atencion;
    }
}
package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.repository.HistorialRepository;
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
class HistorialRepositoryTest {

    @Autowired
    private HistorialRepository repository;

    @Test
    void saveDebeGuardarHistorialCompleto() {
        HistorialClinico guardado =
                repository.saveAndFlush(
                        crearHistorial(
                                10L,
                                LocalDateTime.of(
                                        2026,
                                        6,
                                        20,
                                        10,
                                        0
                                ),
                                "VACUNA",
                                50L
                        )
                );

        assertAll(
                () -> assertNotNull(guardado.getId()),
                () -> assertEquals(
                        10L,
                        guardado.getMascotaId()
                ),
                () -> assertEquals(
                        "VACUNA",
                        guardado.getTipo()
                ),
                () -> assertEquals(
                        50L,
                        guardado.getReferenciaExternaId()
                )
        );
    }

    @Test
    void findAllDebeOrdenarPorFechaDescendente() {
        repository.saveAndFlush(
                crearHistorial(
                        10L,
                        LocalDateTime.of(
                                2026,
                                6,
                                20,
                                10,
                                0
                        ),
                        "VACUNA",
                        50L
                )
        );

        repository.saveAndFlush(
                crearHistorial(
                        20L,
                        LocalDateTime.of(
                                2026,
                                6,
                                22,
                                10,
                                0
                        ),
                        "ATENCION",
                        60L
                )
        );

        List<HistorialClinico> resultado =
                repository.findAllByOrderByFechaDesc();

        assertEquals(2, resultado.size());
        assertEquals(
                20L,
                resultado.get(0).getMascotaId()
        );
    }

    @Test
    void findByMascotaDebeFiltrarYOrdenar() {
        repository.saveAndFlush(
                crearHistorial(
                        10L,
                        LocalDateTime.of(
                                2026,
                                6,
                                20,
                                10,
                                0
                        ),
                        "VACUNA",
                        50L
                )
        );

        repository.saveAndFlush(
                crearHistorial(
                        10L,
                        LocalDateTime.of(
                                2026,
                                6,
                                22,
                                10,
                                0
                        ),
                        "ATENCION",
                        60L
                )
        );

        repository.saveAndFlush(
                crearHistorial(
                        20L,
                        LocalDateTime.of(
                                2026,
                                6,
                                23,
                                10,
                                0
                        ),
                        "EXAMEN",
                        70L
                )
        );

        List<HistorialClinico> resultado =
                repository
                        .findByMascotaIdOrderByFechaDesc(
                                10L
                        );

        assertEquals(2, resultado.size());

        assertTrue(
                resultado.get(0)
                        .getFecha()
                        .isAfter(
                                resultado.get(1).getFecha()
                        )
        );

        assertTrue(
                resultado.stream()
                        .allMatch(historial ->
                                historial.getMascotaId()
                                        .equals(10L)
                        )
        );
    }

    @Test
    void findByTipoYReferenciaDebeEncontrarHistorial() {
        repository.saveAndFlush(
                crearHistorial(
                        10L,
                        LocalDateTime.now(),
                        "ATENCION",
                        500L
                )
        );

        Optional<HistorialClinico> resultado =
                repository
                        .findByTipoAndReferenciaExternaId(
                                "ATENCION",
                                500L
                        );

        assertTrue(resultado.isPresent());
        assertEquals(
                10L,
                resultado.get().getMascotaId()
        );
    }

    @Test
    void findByTipoYReferenciaDebeRetornarVacio() {
        Optional<HistorialClinico> resultado =
                repository
                        .findByTipoAndReferenciaExternaId(
                                "ATENCION",
                                999L
                        );

        assertTrue(resultado.isEmpty());
    }

    private HistorialClinico crearHistorial(
            Long mascotaId,
            LocalDateTime fecha,
            String tipo,
            Long referencia
    ) {
        HistorialClinico historial =
                new HistorialClinico();

        historial.setMascotaId(mascotaId);
        historial.setFecha(fecha);
        historial.setTipo(tipo);
        historial.setDetalle(
                "Detalle clínico"
        );
        historial.setReferenciaExternaId(
                referencia
        );

        return historial;
    }
}
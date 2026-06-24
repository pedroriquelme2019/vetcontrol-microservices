package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.repository.NotificacionRepository;
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
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repository;

    @Test
    void saveDebeGuardarNotificacionCompleta() {
        Notificacion guardada =
                repository.saveAndFlush(
                        crearNotificacion(
                                TipoNotificacion.CITA,
                                LocalDateTime.of(
                                        2026,
                                        6,
                                        20,
                                        10,
                                        0
                                ),
                                false,
                                "cita-creada:1"
                        )
                );

        assertAll(
                () -> assertNotNull(guardada.getId()),
                () -> assertEquals(
                        TipoNotificacion.CITA,
                        guardada.getTipo()
                ),
                () -> assertNotNull(
                        guardada.getFecha()
                ),
                () -> assertFalse(
                        guardada.isLeida()
                ),
                () -> assertEquals(
                        "cita-creada:1",
                        guardada.getClaveEvento()
                )
        );
    }

    @Test
    void prePersistDebeAsignarFechaCuandoEsNula() {
        Notificacion notificacion =
                crearNotificacion(
                        TipoNotificacion.SISTEMA,
                        null,
                        false,
                        null
                );

        Notificacion guardada =
                repository.saveAndFlush(notificacion);

        assertNotNull(guardada.getFecha());
    }

    @Test
    void findAllDebeOrdenarPorFechaDescendente() {
        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.CITA,
                        LocalDateTime.of(
                                2026,
                                6,
                                20,
                                10,
                                0
                        ),
                        false,
                        "cita-creada:1"
                )
        );

        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.VENTA,
                        LocalDateTime.of(
                                2026,
                                6,
                                22,
                                10,
                                0
                        ),
                        false,
                        "venta-creada:2"
                )
        );

        List<Notificacion> resultado =
                repository
                        .findAllByOrderByFechaDesc();

        assertEquals(2, resultado.size());

        assertEquals(
                TipoNotificacion.VENTA,
                resultado.get(0).getTipo()
        );
    }

    @Test
    void findNoLeidasDebeRetornarSoloPendientes() {
        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.CITA,
                        LocalDateTime.now().minusHours(2),
                        false,
                        "cita-creada:1"
                )
        );

        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.VENTA,
                        LocalDateTime.now().minusHours(1),
                        true,
                        "venta-creada:2"
                )
        );

        List<Notificacion> resultado =
                repository
                        .findByLeidaFalseOrderByFechaDesc();

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isLeida());
        assertEquals(
                TipoNotificacion.CITA,
                resultado.get(0).getTipo()
        );
    }

    @Test
    void findByTipoDebeFiltrarYOrdenar() {
        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.CITA,
                        LocalDateTime.now().minusDays(2),
                        false,
                        "cita-creada:1"
                )
        );

        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.CITA,
                        LocalDateTime.now().minusDays(1),
                        false,
                        "cita-creada:2"
                )
        );

        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.VENTA,
                        LocalDateTime.now(),
                        false,
                        "venta-creada:3"
                )
        );

        List<Notificacion> resultado =
                repository
                        .findByTipoOrderByFechaDesc(
                                TipoNotificacion.CITA
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
                        .allMatch(notificacion ->
                                notificacion.getTipo()
                                        == TipoNotificacion.CITA
                        )
        );
    }

    @Test
    void findByClaveEventoDebeEncontrarRegistro() {
        repository.saveAndFlush(
                crearNotificacion(
                        TipoNotificacion.ATENCION,
                        LocalDateTime.now(),
                        false,
                        "atencion-registrada:50"
                )
        );

        Optional<Notificacion> resultado =
                repository.findByClaveEvento(
                        "atencion-registrada:50"
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                TipoNotificacion.ATENCION,
                resultado.get().getTipo()
        );
    }

    @Test
    void findByClaveEventoDebeRetornarVacio() {
        Optional<Notificacion> resultado =
                repository.findByClaveEvento(
                        "venta-creada:999"
                );

        assertTrue(resultado.isEmpty());
    }

    private Notificacion crearNotificacion(
            TipoNotificacion tipo,
            LocalDateTime fecha,
            boolean leida,
            String claveEvento
    ) {
        Notificacion notificacion =
                new Notificacion();

        notificacion.setTipo(tipo);
        notificacion.setMensaje(
                "Mensaje de prueba"
        );
        notificacion.setFecha(fecha);
        notificacion.setLeida(leida);
        notificacion.setOrigenEvento(
                "EVENTO"
        );
        notificacion.setReferenciaExternaId(1L);
        notificacion.setClaveEvento(
                claveEvento
        );

        return notificacion;
    }
}
package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.exception.BusinessException;
import cl.duoc.vetcontrol.notificacion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.repository.NotificacionRepository;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificacionServiceTest {

    private NotificacionRepository repository;
    private NotificacionService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificacionRepository.class);
        service = new NotificacionService(repository);
    }

    @Test
    void findAllDebeRetornarNotificacionesOrdenadas() {
        when(repository.findAllByOrderByFechaDesc())
                .thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado =
                service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(
                TipoNotificacion.CITA,
                resultado.get(0).getTipo()
        );

        verify(repository)
                .findAllByOrderByFechaDesc();
    }

    @Test
    void findByIdDebeRetornarNotificacion() {
        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(crearNotificacion())
                );

        Notificacion resultado =
                service.findById(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        TipoNotificacion.CITA,
                        resultado.getTipo()
                ),
                () -> assertFalse(resultado.isLeida())
        );
    }

    @Test
    void findByIdDebeFallarCuandoNoExiste() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findById(99L)
                );

        assertEquals(
                "Notificación no encontrada: 99",
                exception.getMessage()
        );
    }

    @Test
    void findNoLeidasDebeRetornarPendientes() {
        when(repository
                .findByLeidaFalseOrderByFechaDesc())
                .thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado =
                service.findNoLeidas();

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isLeida());

        verify(repository)
                .findByLeidaFalseOrderByFechaDesc();
    }

    @Test
    void findByTipoDebeNormalizarTexto() {
        when(repository.findByTipoOrderByFechaDesc(
                TipoNotificacion.VENTA
        )).thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado =
                service.findByTipo("  venta  ");

        assertEquals(1, resultado.size());

        verify(repository)
                .findByTipoOrderByFechaDesc(
                        TipoNotificacion.VENTA
                );
    }

    @Test
    void findByTipoDebeRechazarTipoNulo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.findByTipo(null)
                );

        assertEquals(
                "El tipo de notificación es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void findByTipoDebeRechazarTipoVacio() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.findByTipo("   ")
                );

        assertEquals(
                "El tipo de notificación es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void findByTipoDebeRechazarTipoDesconocido() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.findByTipo("OTRO")
                );

        assertEquals(
                "Tipo de notificación no permitido",
                exception.getMessage()
        );
    }

    @Test
    void createDebeGuardarNotificacionManual() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "  Recordatorio de control  "
                );

        when(repository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> {
                    Notificacion notificacion =
                            invocation.getArgument(0);

                    notificacion.setId(1L);

                    return notificacion;
                });

        Notificacion resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        TipoNotificacion.MANUAL,
                        resultado.getTipo()
                ),
                () -> assertEquals(
                        "Recordatorio de control",
                        resultado.getMensaje()
                ),
                () -> assertEquals(
                        "API",
                        resultado.getOrigenEvento()
                ),
                () -> assertFalse(resultado.isLeida()),
                () -> assertNull(
                        resultado.getReferenciaExternaId()
                ),
                () -> assertNull(
                        resultado.getClaveEvento()
                )
        );

        verify(repository)
                .save(any(Notificacion.class));
    }

    @Test
    void createDebeRechazarMensajeNulo() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "El mensaje es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeRechazarMensajeVacio() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "   "
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "El mensaje es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarMensajeSuperiorA500Caracteres() {
        NotificacionRequest request =
                new NotificacionRequest(
                        TipoNotificacion.MANUAL,
                        "a".repeat(501)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "El mensaje no puede superar los 500 caracteres",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeEventoDebeCrearNotificacion() {
        when(repository.findByClaveEvento(
                "venta-creada:50"
        )).thenReturn(Optional.empty());

        when(repository.saveAndFlush(
                any(Notificacion.class)
        )).thenAnswer(invocation -> {
            Notificacion notificacion =
                    invocation.getArgument(0);

            notificacion.setId(1L);

            return notificacion;
        });

        Notificacion resultado =
                service.registrarDesdeEvento(
                        TipoNotificacion.VENTA,
                        "  Nueva venta registrada  ",
                        "  venta-creada  ",
                        50L
                );

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(
                        TipoNotificacion.VENTA,
                        resultado.getTipo()
                ),
                () -> assertEquals(
                        "Nueva venta registrada",
                        resultado.getMensaje()
                ),
                () -> assertEquals(
                        "venta-creada",
                        resultado.getOrigenEvento()
                ),
                () -> assertEquals(
                        50L,
                        resultado.getReferenciaExternaId()
                ),
                () -> assertEquals(
                        "venta-creada:50",
                        resultado.getClaveEvento()
                ),
                () -> assertFalse(resultado.isLeida())
        );
    }

    @Test
    void registrarDesdeEventoDebeIgnorarDuplicado() {
        Notificacion existente =
                crearNotificacion();

        existente.setClaveEvento(
                "cita-creada:20"
        );

        when(repository.findByClaveEvento(
                "cita-creada:20"
        )).thenReturn(Optional.of(existente));

        Notificacion resultado =
                service.registrarDesdeEvento(
                        TipoNotificacion.CITA,
                        "Nueva cita",
                        "cita-creada",
                        20L
                );

        assertSame(existente, resultado);

        verify(repository, never())
                .saveAndFlush(any());
    }

    @Test
    void registrarDesdeEventoDebeRecuperarRegistroAnteCondicionDeCarrera() {
        Notificacion existente =
                crearNotificacion();

        existente.setClaveEvento(
                "venta-creada:50"
        );

        when(repository.findByClaveEvento(
                "venta-creada:50"
        )).thenReturn(
                Optional.empty(),
                Optional.of(existente)
        );

        when(repository.saveAndFlush(
                any(Notificacion.class)
        )).thenThrow(
                new DataIntegrityViolationException(
                        "Clave duplicada"
                )
        );

        Notificacion resultado =
                service.registrarDesdeEvento(
                        TipoNotificacion.VENTA,
                        "Venta registrada",
                        "venta-creada",
                        50L
                );

        assertSame(existente, resultado);

        verify(repository, times(2))
                .findByClaveEvento(
                        "venta-creada:50"
                );
    }

    @Test
    void registrarDesdeEventoDebeRelanzarErrorSiNoEncuentraDuplicado() {
        DataIntegrityViolationException error =
                new DataIntegrityViolationException(
                        "Clave duplicada"
                );

        when(repository.findByClaveEvento(
                "venta-creada:50"
        )).thenReturn(
                Optional.empty(),
                Optional.empty()
        );

        when(repository.saveAndFlush(
                any(Notificacion.class)
        )).thenThrow(error);

        DataIntegrityViolationException resultado =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> service.registrarDesdeEvento(
                                TipoNotificacion.VENTA,
                                "Venta registrada",
                                "venta-creada",
                                50L
                        )
                );

        assertSame(error, resultado);
    }

    @Test
    void registrarDesdeEventoDebeRechazarTipoNulo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeEvento(
                                null,
                                "Mensaje",
                                "venta-creada",
                                50L
                        )
                );

        assertEquals(
                "El tipo del evento es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeEventoDebeRechazarOrigenNulo() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeEvento(
                                TipoNotificacion.VENTA,
                                "Mensaje",
                                null,
                                50L
                        )
                );

        assertEquals(
                "El origen del evento es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeEventoDebeRechazarOrigenVacio() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeEvento(
                                TipoNotificacion.VENTA,
                                "Mensaje",
                                "   ",
                                50L
                        )
                );

        assertEquals(
                "El origen del evento es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeEventoDebeRechazarReferenciaNula() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeEvento(
                                TipoNotificacion.VENTA,
                                "Mensaje",
                                "venta-creada",
                                null
                        )
                );

        assertEquals(
                "La referencia externa debe ser mayor que cero",
                exception.getMessage()
        );
    }

    @Test
    void registrarDesdeEventoDebeRechazarReferenciaCero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.registrarDesdeEvento(
                                TipoNotificacion.VENTA,
                                "Mensaje",
                                "venta-creada",
                                0L
                        )
                );

        assertEquals(
                "La referencia externa debe ser mayor que cero",
                exception.getMessage()
        );
    }

    @Test
    void marcarComoLeidaDebeActualizarNotificacion() {
        Notificacion notificacion =
                crearNotificacion();

        when(repository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        when(repository.save(notificacion))
                .thenReturn(notificacion);

        Notificacion resultado =
                service.marcarComoLeida(1L);

        assertTrue(resultado.isLeida());

        verify(repository)
                .save(notificacion);
    }

    @Test
    void marcarComoLeidaNoDebeGuardarNuevamenteSiYaEstabaLeida() {
        Notificacion notificacion =
                crearNotificacion();

        notificacion.setLeida(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        Notificacion resultado =
                service.marcarComoLeida(1L);

        assertTrue(resultado.isLeida());

        verify(repository, never())
                .save(any());
    }

    @Test
    void marcarTodasComoLeidasDebeActualizarPendientes() {
        Notificacion primera =
                crearNotificacion();

        Notificacion segunda =
                crearNotificacion();

        segunda.setId(2L);

        when(repository
                .findByLeidaFalseOrderByFechaDesc())
                .thenReturn(
                        List.of(primera, segunda)
                );

        int cantidad =
                service.marcarTodasComoLeidas();

        assertEquals(2, cantidad);
        assertTrue(primera.isLeida());
        assertTrue(segunda.isLeida());

        verify(repository)
                .saveAll(
                        List.of(primera, segunda)
                );
    }

    @Test
    void marcarTodasComoLeidasNoDebeGuardarListaVacia() {
        when(repository
                .findByLeidaFalseOrderByFechaDesc())
                .thenReturn(List.of());

        int cantidad =
                service.marcarTodasComoLeidas();

        assertEquals(0, cantidad);

        verify(repository, never())
                .saveAll(any());
    }

    @Test
    void deleteDebeEliminarNotificacionExistente() {
        Notificacion notificacion =
                crearNotificacion();

        when(repository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        service.delete(1L);

        verify(repository)
                .delete(notificacion);
    }

    private Notificacion crearNotificacion() {
        Notificacion notificacion =
                new Notificacion();

        notificacion.setId(1L);
        notificacion.setTipo(
                TipoNotificacion.CITA
        );
        notificacion.setMensaje(
                "Nueva cita registrada"
        );
        notificacion.setOrigenEvento(
                "cita-creada"
        );
        notificacion.setReferenciaExternaId(20L);
        notificacion.setClaveEvento(
                "cita-creada:20"
        );
        notificacion.setLeida(false);

        return notificacion;
    }
}
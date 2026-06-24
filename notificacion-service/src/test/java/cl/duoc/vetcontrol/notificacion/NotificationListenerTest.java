package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.config.KafkaConfig;
import cl.duoc.vetcontrol.notificacion.messaging.NotificationListener;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationListenerTest {

    private NotificacionService service;
    private NotificationListener listener;

    @BeforeEach
    void setUp() {
        service =
                mock(NotificacionService.class);

        listener =
                new NotificationListener(
                        service,
                        new ObjectMapper()
                );
    }

    @Test
    void eventoCitaDebeRegistrarNotificacion() {
        when(service.registrarDesdeEvento(
                any(),
                anyString(),
                anyString(),
                anyLong()
        )).thenReturn(new Notificacion());

        String payload = """
                {
                  "tipo": "CITA_CREADA",
                  "citaId": 10,
                  "mascotaId": 20,
                  "fecha": "2026-06-25",
                  "hora": "10:30"
                }
                """;

        listener.cita(payload);

        ArgumentCaptor<String> mensajeCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(service)
                .registrarDesdeEvento(
                        eq(TipoNotificacion.CITA),
                        mensajeCaptor.capture(),
                        eq(
                                KafkaConfig.TOPIC_CITA_CREADA
                        ),
                        eq(10L)
                );

        assertEquals(
                "Nueva cita registrada. Cita #10, mascota #20, fecha 2026-06-25 10:30",
                mensajeCaptor.getValue()
        );
    }

    @Test
    void eventoVentaDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "VENTA_CREADA",
                  "ventaId": 50,
                  "clienteId": 30,
                  "total": 15990
                }
                """;

        listener.venta(payload);

        ArgumentCaptor<String> mensajeCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(service)
                .registrarDesdeEvento(
                        eq(TipoNotificacion.VENTA),
                        mensajeCaptor.capture(),
                        eq(
                                KafkaConfig.TOPIC_VENTA_CREADA
                        ),
                        eq(50L)
                );

        assertEquals(
                "Nueva venta registrada. Venta #50, cliente #30, total $15990",
                mensajeCaptor.getValue()
        );
    }

    @Test
    void eventoAtencionDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 70,
                  "mascotaId": 20
                }
                """;

        listener.atencion(payload);

        ArgumentCaptor<String> mensajeCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(service)
                .registrarDesdeEvento(
                        eq(TipoNotificacion.ATENCION),
                        mensajeCaptor.capture(),
                        eq(
                                KafkaConfig.TOPIC_ATENCION_REGISTRADA
                        ),
                        eq(70L)
                );

        assertEquals(
                "Nueva atención registrada. Atención #70, mascota #20",
                mensajeCaptor.getValue()
        );
    }

    @Test
    void eventoSinTipoDebeSerAceptado() {
        String payload = """
                {
                  "ventaId": 50
                }
                """;

        listener.venta(payload);

        verify(service)
                .registrarDesdeEvento(
                        TipoNotificacion.VENTA,
                        "Nueva venta registrada. Venta #50",
                        KafkaConfig.TOPIC_VENTA_CREADA,
                        50L
                );
    }

    @Test
    void jsonInvalidoNoDebeRegistrarNotificacion() {
        listener.cita(
                "{json-invalido"
        );

        verifyNoInteractions(service);
    }

    @Test
    void arregloJsonNoDebeRegistrarNotificacion() {
        listener.cita(
                "[1, 2, 3]"
        );

        verifyNoInteractions(service);
    }

    @Test
    void tipoIncorrectoNoDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "VENTA_CREADA",
                  "citaId": 10
                }
                """;

        listener.cita(payload);

        verifyNoInteractions(service);
    }

    @Test
    void identificadorFaltanteNoDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "VENTA_CREADA"
                }
                """;

        listener.venta(payload);

        verifyNoInteractions(service);
    }

    @Test
    void identificadorTextoNoDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "VENTA_CREADA",
                  "ventaId": "abc"
                }
                """;

        listener.venta(payload);

        verifyNoInteractions(service);
    }

    @Test
    void identificadorCeroNoDebeRegistrarNotificacion() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 0
                }
                """;

        listener.atencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void errorDelServicioDebeSerControlado() {
        doThrow(
                new RuntimeException(
                        "Error de base de datos"
                )
        ).when(service)
                .registrarDesdeEvento(
                        any(),
                        anyString(),
                        anyString(),
                        anyLong()
                );

        String payload = """
                {
                  "tipo": "VENTA_CREADA",
                  "ventaId": 50
                }
                """;

        assertDoesNotThrow(
                () -> listener.venta(payload)
        );

        verify(service)
                .registrarDesdeEvento(
                        TipoNotificacion.VENTA,
                        "Nueva venta registrada. Venta #50",
                        KafkaConfig.TOPIC_VENTA_CREADA,
                        50L
                );
    }
}
package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.messaging.HistorialEventListener;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class HistorialEventListenerTest {

    private HistorialService service;
    private HistorialEventListener listener;

    @BeforeEach
    void setUp() {
        service = mock(HistorialService.class);

        listener = new HistorialEventListener(
                service,
                new ObjectMapper()
        );
    }

    @Test
    void eventoValidoDebeRegistrarHistorial() {
        HistorialClinico historial =
                new HistorialClinico();

        historial.setId(1L);

        when(service.registrarDesdeAtencion(
                10L,
                500L
        )).thenReturn(historial);

        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 500,
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verify(service)
                .registrarDesdeAtencion(
                        10L,
                        500L
                );
    }

    @Test
    void jsonInvalidoNoDebeLlamarServicio() {
        listener.onAtencion(
                "{json-invalido"
        );

        verifyNoInteractions(service);
    }

    @Test
    void tipoIncorrectoNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "VENTA_CREADA",
                  "atencionId": 500,
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void atencionIdFaltanteNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void atencionIdTextoNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": "abc",
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void atencionIdCeroNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 0,
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void mascotaIdFaltanteNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 500
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void mascotaIdNegativoNoDebeLlamarServicio() {
        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 500,
                  "mascotaId": -1
                }
                """;

        listener.onAtencion(payload);

        verifyNoInteractions(service);
    }

    @Test
    void errorDelServicioDebeSerControlado() {
        when(service.registrarDesdeAtencion(
                10L,
                500L
        )).thenThrow(
                new RuntimeException(
                        "Error de base de datos"
                )
        );

        String payload = """
                {
                  "tipo": "ATENCION_REGISTRADA",
                  "atencionId": 500,
                  "mascotaId": 10
                }
                """;

        listener.onAtencion(payload);

        verify(service)
                .registrarDesdeAtencion(
                        10L,
                        500L
                );
    }
}
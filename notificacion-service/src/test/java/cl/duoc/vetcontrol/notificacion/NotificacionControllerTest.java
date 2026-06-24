package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.controller.NotificacionController;
import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.exception.BusinessException;
import cl.duoc.vetcontrol.notificacion.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.notificacion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService service;

    @Test
    void findAllDebeRetornar200() throws Exception {
        when(service.findAll())
                .thenReturn(
                        List.of(crearNotificacion())
                );

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].tipo")
                                .value("CITA")
                );
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {
        when(service.findById(1L))
                .thenReturn(crearNotificacion());

        mockMvc.perform(
                        get("/api/v1/notificaciones/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.leida")
                                .value(false)
                );
    }

    @Test
    void findNoLeidasDebeRetornar200() throws Exception {
        when(service.findNoLeidas())
                .thenReturn(
                        List.of(crearNotificacion())
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notificaciones/no-leidas"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].leida")
                                .value(false)
                );
    }

    @Test
    void findByTipoDebeRetornar200() throws Exception {
        when(service.findByTipo("cita"))
                .thenReturn(
                        List.of(crearNotificacion())
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notificaciones/tipos/cita"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].tipo")
                                .value("CITA")
                );
    }

    @Test
    void createDebeRetornar201() throws Exception {
        when(service.create(
                any(NotificacionRequest.class)
        )).thenReturn(crearNotificacion());

        mockMvc.perform(
                        post("/api/v1/notificaciones")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                );
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        NotificacionRequest request =
                new NotificacionRequest(
                        null,
                        ""
                );

        mockMvc.perform(
                        post("/api/v1/notificaciones")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Error de validación"
                                )
                )
                .andExpect(
                        jsonPath("$.details")
                                .isArray()
                );

        verifyNoInteractions(service);
    }

    @Test
    void tipoJsonInvalidoDebeRetornar400()
            throws Exception {

        String json = """
                {
                  "tipo": "DESCONOCIDO",
                  "mensaje": "Mensaje"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/notificaciones")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Cuerpo de solicitud inválido"
                                )
                );

        verifyNoInteractions(service);
    }

    @Test
    void marcarComoLeidaDebeRetornar200()
            throws Exception {

        Notificacion notificacion =
                crearNotificacion();

        notificacion.setLeida(true);

        when(service.marcarComoLeida(1L))
                .thenReturn(notificacion);

        mockMvc.perform(
                        put(
                                "/api/v1/notificaciones/1/leer"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.leida")
                                .value(true)
                );
    }

    @Test
    void marcarTodasComoLeidasDebeRetornarCantidad()
            throws Exception {

        when(service.marcarTodasComoLeidas())
                .thenReturn(3);

        mockMvc.perform(
                        put(
                                "/api/v1/notificaciones/leer-todas"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void deleteDebeRetornar204() throws Exception {
        doNothing()
                .when(service)
                .delete(1L);

        mockMvc.perform(
                        delete(
                                "/api/v1/notificaciones/1"
                        )
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void notificacionNoEncontradaDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Notificación no encontrada: 99"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notificaciones/99"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Notificación no encontrada: 99"
                                )
                );
    }

    @Test
    void tipoNoPermitidoDebeRetornar400()
            throws Exception {

        when(service.findByTipo("otro"))
                .thenThrow(
                        new BusinessException(
                                "Tipo de notificación no permitido"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notificaciones/tipos/otro"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Tipo de notificación no permitido"
                                )
                );
    }

    @Test
    void errorGeneralDebeRetornar500()
            throws Exception {

        when(service.findAll())
                .thenThrow(
                        new RuntimeException(
                                "Error MySQL"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(
                        status()
                                .isInternalServerError()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Error interno del servidor"
                                )
                );
    }

    private NotificacionRequest crearRequest() {
        return new NotificacionRequest(
                TipoNotificacion.MANUAL,
                "Mensaje manual"
        );
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
        notificacion.setLeida(false);

        return notificacion;
    }
}
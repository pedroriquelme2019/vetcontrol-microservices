package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.controller.NotificacionController;
import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.security.JwtAuthFilter;
import cl.duoc.vetcontrol.notificacion.security.SecurityConfig;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class
})
@TestPropertySource(properties = {
        "security.jwt.secret=vetcontrol-secret-key-2026-vetcontrol-secret-key-2026"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeConsultar()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeConsultar()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeConsultar()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComunNoPuedeConsultar()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/notificaciones")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrear()
            throws Exception {

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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeCrear()
            throws Exception {

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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeCrear()
            throws Exception {

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
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeMarcarComoLeida()
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
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminar()
            throws Exception {

        doNothing()
                .when(service)
                .delete(1L);

        mockMvc.perform(
                        delete(
                                "/api/v1/notificaciones/1"
                        )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeEliminar()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/v1/notificaciones/1"
                        )
                )
                .andExpect(status().isForbidden());

        verify(service, never())
                .delete(anyLong());
    }

    private NotificacionRequest crearRequest() {
        return new NotificacionRequest(
                TipoNotificacion.MANUAL,
                "Mensaje"
        );
    }

    private Notificacion crearNotificacion() {
        Notificacion notificacion =
                new Notificacion();

        notificacion.setId(1L);
        notificacion.setTipo(
                TipoNotificacion.MANUAL
        );
        notificacion.setMensaje("Mensaje");

        return notificacion;
    }
}
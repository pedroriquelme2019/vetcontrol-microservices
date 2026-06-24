package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.controller.HistorialController;
import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.security.JwtAuthFilter;
import cl.duoc.vetcontrol.historial.security.SecurityConfig;
import cl.duoc.vetcontrol.historial.service.HistorialService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistorialController.class)
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
    private HistorialService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeConsultar()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/historiales")
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
                        get("/api/v1/historiales")
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
                        get("/api/v1/historiales")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComunNoPuedeConsultar()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/historiales")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrear()
            throws Exception {

        when(service.create(
                any(HistorialRequest.class)
        )).thenReturn(crearHistorial());

        mockMvc.perform(
                        post("/api/v1/historiales")
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
    void veterinarioPuedeCrear()
            throws Exception {

        when(service.create(
                any(HistorialRequest.class)
        )).thenReturn(crearHistorial());

        mockMvc.perform(
                        post("/api/v1/historiales")
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
    void recepcionistaNoPuedeCrear()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/historiales")
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

    private HistorialRequest crearRequest() {
        return new HistorialRequest(
                10L,
                LocalDateTime.now().minusHours(1),
                "VACUNA",
                "Vacuna aplicada",
                null
        );
    }

    private HistorialClinico crearHistorial() {
        HistorialClinico historial =
                new HistorialClinico();

        historial.setId(1L);
        historial.setMascotaId(10L);
        historial.setFecha(
                LocalDateTime.now().minusHours(1)
        );
        historial.setTipo("VACUNA");
        historial.setDetalle(
                "Vacuna aplicada"
        );

        return historial;
    }
}
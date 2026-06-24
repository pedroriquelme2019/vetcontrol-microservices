package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.controller.AtencionController;
import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.security.JwtAuthFilter;
import cl.duoc.vetcontrol.atencion.security.SecurityConfig;
import cl.duoc.vetcontrol.atencion.service.AtencionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtencionController.class)
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
    private AtencionService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeConsultar() throws Exception {

        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/atenciones"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeConsultar() throws Exception {

        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/atenciones"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComunNoPuedeConsultar() throws Exception {

        mockMvc.perform(get("/api/v1/atenciones"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeCrear() throws Exception {

        when(service.create(any(AtencionRequest.class)))
                .thenReturn(crearAtencion());

        mockMvc.perform(
                        post("/api/v1/atenciones")
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
    void recepcionistaNoPuedeCrear() throws Exception {

        mockMvc.perform(
                        post("/api/v1/atenciones")
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
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeActualizar() throws Exception {

        when(service.update(
                eq(1L),
                any(AtencionRequest.class)
        )).thenReturn(crearAtencion());

        mockMvc.perform(
                        put("/api/v1/atenciones/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeActualizar() throws Exception {

        when(service.update(
                eq(1L),
                any(AtencionRequest.class)
        )).thenReturn(crearAtencion());

        mockMvc.perform(
                        put("/api/v1/atenciones/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminar() throws Exception {

        mockMvc.perform(
                        delete("/api/v1/atenciones/1")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeEliminar() throws Exception {

        mockMvc.perform(
                        delete("/api/v1/atenciones/1")
                )
                .andExpect(status().isForbidden());
    }

    private AtencionRequest crearRequest() {
        return new AtencionRequest(
                5L,
                10L,
                20L,
                LocalDateTime.now().minusHours(1),
                "Dermatitis",
                "Antihistamínico",
                "Control"
        );
    }

    private Atencion crearAtencion() {

        Atencion atencion = new Atencion();

        atencion.setId(1L);
        atencion.setCitaId(5L);
        atencion.setMascotaId(10L);
        atencion.setVeterinarioId(20L);
        atencion.setFechaAtencion(
                LocalDateTime.now().minusHours(1)
        );
        atencion.setDiagnostico("Dermatitis");
        atencion.setTratamiento("Antihistamínico");

        return atencion;
    }
}
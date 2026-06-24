package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.controller.CitaController;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import cl.duoc.vetcontrol.agenda.security.JwtAuthFilter;
import cl.duoc.vetcontrol.agenda.security.SecurityConfig;
import cl.duoc.vetcontrol.agenda.service.CitaService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaController.class)
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
    private CitaService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeConsultar() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeConsultar() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComunNoPuedeConsultar() throws Exception {
        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeCrear() throws Exception {
        when(service.create(any(CitaRequest.class)))
                .thenReturn(crearCita());

        mockMvc.perform(
                        post("/api/v1/citas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrear() throws Exception {
        when(service.create(any(CitaRequest.class)))
                .thenReturn(crearCita());

        mockMvc.perform(
                        post("/api/v1/citas")
                                .contentType(MediaType.APPLICATION_JSON)
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
    void veterinarioNoPuedeCrear() throws Exception {
        mockMvc.perform(
                        post("/api/v1/citas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/citas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/citas/1"))
                .andExpect(status().isForbidden());
    }

    private CitaRequest crearRequest() {
        return new CitaRequest(
                10L,
                20L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Control"
        );
    }

    private Cita crearCita() {
        Cita cita = new Cita();

        cita.setId(1L);
        cita.setMascotaId(10L);
        cita.setVeterinarioId(20L);
        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        cita.setMotivo("Control");
        cita.setEstado(EstadoCita.PROGRAMADA);

        return cita;
    }
}
package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.controller.MascotaController;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.security.JwtAuthFilter;
import cl.duoc.vetcontrol.mascota.security.SecurityConfig;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MascotaController.class)
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
    private MascotaService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeRealizarGet()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeRealizarGet()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioNoAutorizadoNoPuedeRealizarGet()
            throws Exception {

        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeCrearMascota()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/mascotas")
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
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeCrearMascota()
            throws Exception {

        when(service.create(any(MascotaRequest.class)))
                .thenReturn(crearMascota());

        mockMvc.perform(
                        post("/api/v1/mascotas")
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
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrearMascota()
            throws Exception {

        when(service.create(any(MascotaRequest.class)))
                .thenReturn(crearMascota());

        mockMvc.perform(
                        post("/api/v1/mascotas")
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

    private MascotaRequest crearRequest() {
        return new MascotaRequest(
                1L,
                "Firulais",
                "Perro",
                "Labrador",
                5,
                "Macho",
                18.5,
                "CHIP-001"
        );
    }

    private Mascota crearMascota() {

        Mascota mascota = new Mascota();

        mascota.setId(1L);
        mascota.setClienteId(1L);
        mascota.setNombre("Firulais");
        mascota.setEspecie("Perro");

        return mascota;
    }
}
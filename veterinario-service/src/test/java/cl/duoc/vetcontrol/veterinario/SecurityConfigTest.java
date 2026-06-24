package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.controller.VeterinarioController;
import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.security.JwtAuthFilter;
import cl.duoc.vetcontrol.veterinario.security.SecurityConfig;
import cl.duoc.vetcontrol.veterinario.service.VeterinarioService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VeterinarioController.class)
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
    private VeterinarioService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void getDebePermitirseParaVeterinario() throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/veterinarios")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void getDebePermitirseParaRecepcionista() throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/veterinarios")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDebeDenegarseParaRolNoAutorizado() throws Exception {

        mockMvc.perform(
                        get("/api/v1/veterinarios")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void postDebeDenegarseParaVeterinario() throws Exception {

        VeterinarioRequest request =
                crearRequest();

        mockMvc.perform(
                        post("/api/v1/veterinarios")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void postDebePermitirseParaRecepcionista() throws Exception {

        VeterinarioRequest request =
                crearRequest();

        when(service.create(any(VeterinarioRequest.class)))
                .thenReturn(crearVeterinario());

        mockMvc.perform(
                        post("/api/v1/veterinarios")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postDebePermitirseParaAdmin() throws Exception {

        VeterinarioRequest request =
                crearRequest();

        when(service.create(any(VeterinarioRequest.class)))
                .thenReturn(crearVeterinario());

        mockMvc.perform(
                        post("/api/v1/veterinarios")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isCreated());
    }

    private VeterinarioRequest crearRequest() {

        return new VeterinarioRequest(
                "11111111-1",
                "Juan Pérez",
                "Cirugía",
                "juan@correo.cl"
        );
    }

    private Veterinario crearVeterinario() {

        Veterinario veterinario =
                new Veterinario();

        veterinario.setId(1L);
        veterinario.setRut("11111111-1");
        veterinario.setNombre("Juan Pérez");
        veterinario.setEspecialidad("Cirugía");
        veterinario.setCorreo("juan@correo.cl");

        return veterinario;
    }
}
package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.controller.VeterinarioController;
import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.service.VeterinarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VeterinarioController.class)
@TestPropertySource(properties = {
        "security.jwt.secret=12345678901234567890123456789012"
})
class VeterinarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VeterinarioService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllDebeRetornar200YListaDeVeterinarios() throws Exception {

        Veterinario veterinario = crearVeterinario();

        when(service.findAll())
                .thenReturn(List.of(veterinario));

        mockMvc.perform(get("/api/v1/veterinarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rut").value("11111111-1"))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$[0].especialidad").value("Cirugía"))
                .andExpect(jsonPath("$[0].correo").value("juan@correo.cl"))
                .andExpect(jsonPath("$[0].activo").value(true));

        verify(service).findAll();
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void findByIdDebeRetornar200YVeterinario() throws Exception {

        Veterinario veterinario = crearVeterinario();

        when(service.findById(1L))
                .thenReturn(veterinario);

        mockMvc.perform(get("/api/v1/veterinarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.especialidad").value("Cirugía"));

        verify(service).findById(1L);
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void buscarPorEspecialidadDebeRetornar200YResultados() throws Exception {

        Veterinario veterinario = crearVeterinario();

        when(service.byEspecialidad("Cirugía"))
                .thenReturn(List.of(veterinario));

        mockMvc.perform(get("/api/v1/veterinarios/especialidad")
                        .param("nombre", "Cirugía"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$[0].especialidad").value("Cirugía"));

        verify(service).byEspecialidad("Cirugía");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDebeRetornar201YVeterinarioCreado() throws Exception {

        VeterinarioRequest request = crearRequest();
        Veterinario veterinario = crearVeterinario();

        when(service.create(any(VeterinarioRequest.class)))
                .thenReturn(veterinario);

        mockMvc.perform(post("/api/v1/veterinarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.correo").value("juan@correo.cl"));

        verify(service).create(any(VeterinarioRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDebeRetornar200YVeterinarioActualizado() throws Exception {

        VeterinarioRequest request = new VeterinarioRequest(
                "11111111-1",
                "Juan Pérez Actualizado",
                "Dermatología",
                "juan.actualizado@correo.cl"
        );

        Veterinario veterinario = crearVeterinario();
        veterinario.setNombre("Juan Pérez Actualizado");
        veterinario.setEspecialidad("Dermatología");
        veterinario.setCorreo("juan.actualizado@correo.cl");

        when(service.update(eq(1L), any(VeterinarioRequest.class)))
                .thenReturn(veterinario);

        mockMvc.perform(put("/api/v1/veterinarios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.especialidad").value("Dermatología"))
                .andExpect(jsonPath("$.correo").value("juan.actualizado@correo.cl"));

        verify(service).update(eq(1L), any(VeterinarioRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/veterinarios/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createConDatosInvalidosDebeRetornar400() throws Exception {

        VeterinarioRequest requestInvalido = new VeterinarioRequest(
                "",
                "",
                "",
                "correo-invalido"
        );

        mockMvc.perform(post("/api/v1/veterinarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void peticionSinAutenticacionDebeRetornar401() throws Exception {

        mockMvc.perform(get("/api/v1/veterinarios"))
                .andExpect(status().isUnauthorized());
    }

    private Veterinario crearVeterinario() {

        Veterinario veterinario = new Veterinario();
        veterinario.setId(1L);
        veterinario.setRut("11111111-1");
        veterinario.setNombre("Juan Pérez");
        veterinario.setEspecialidad("Cirugía");
        veterinario.setCorreo("juan@correo.cl");
        veterinario.setActivo(true);

        return veterinario;
    }

    private VeterinarioRequest crearRequest() {

        return new VeterinarioRequest(
                "11111111-1",
                "Juan Pérez",
                "Cirugía",
                "juan@correo.cl"
        );
    }
}
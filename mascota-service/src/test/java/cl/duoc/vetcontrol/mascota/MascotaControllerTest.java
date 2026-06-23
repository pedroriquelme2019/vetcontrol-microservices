package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.controller.MascotaController;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
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
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MascotaController.class)
@TestPropertySource(properties = {
        "security.jwt.secret=12345678901234567890123456789012"
})
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MascotaService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllDebeRetornar200() throws Exception {

        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setNombre("Firulais");

        when(service.findAll()).thenReturn(List.of(mascota));

        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Firulais"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByIdDebeRetornar200() throws Exception {

        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setNombre("Luna");

        when(service.findById(1L)).thenReturn(mascota);

        mockMvc.perform(get("/api/v1/mascotas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Luna"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByClienteDebeRetornar200() throws Exception {

        Mascota mascota = new Mascota();
        mascota.setNombre("Rocky");

        when(service.findByCliente(10L))
                .thenReturn(List.of(mascota));

        mockMvc.perform(get("/api/v1/mascotas/cliente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Rocky"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDebeRetornar201() throws Exception {

        MascotaRequest request =
                new MascotaRequest(
                        1L,
                        "Firulais",
                        "Perro",
                        "Labrador",
                        3,
                        "Macho",
                        20.0,
                        "MC123"
                );

        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais");

        when(service.create(any(MascotaRequest.class)))
                .thenReturn(mascota);

        mockMvc.perform(post("/api/v1/mascotas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDebeRetornar200() throws Exception {

        MascotaRequest request =
                new MascotaRequest(
                        1L,
                        "Firulais Nuevo",
                        "Perro",
                        "Labrador",
                        4,
                        "Macho",
                        22.0,
                        "MC999"
                );

        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais Nuevo");

        when(service.update(eq(1L), any(MascotaRequest.class)))
                .thenReturn(mascota);

        mockMvc.perform(put("/api/v1/mascotas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/mascotas/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
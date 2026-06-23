package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.controller.ClienteController;
import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(cl.duoc.vetcontrol.cliente.security.SecurityConfig.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllDebeRetornar200() throws Exception {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Pedro");

        when(service.findAll()).thenReturn(List.of(cliente));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pedro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByIdDebeRetornar200() throws Exception {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Maria");

        when(service.findById(1L)).thenReturn(cliente);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maria"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchDebeRetornarResultados() throws Exception {

        Cliente cliente = new Cliente();
        cliente.setNombre("Pedro");

        when(service.search("Pedro")).thenReturn(List.of(cliente));

        mockMvc.perform(get("/api/v1/clientes/buscar")
                        .param("nombre", "Pedro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pedro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDebeRetornar201() throws Exception {

        ClienteRequest request = new ClienteRequest(
                "11111111-1",
                "Pedro",
                "+56911111111",
                "pedro@test.cl",
                "Santiago"
        );

        Cliente cliente = new Cliente();
        cliente.setNombre("Pedro");

        when(service.create(any())).thenReturn(cliente);

        mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDebeRetornar200() throws Exception {

        ClienteRequest request = new ClienteRequest(
                "11111111-1",
                "Pedro Nuevo",
                "+56911111111",
                "nuevo@test.cl",
                "Santiago"
        );

        Cliente cliente = new Cliente();
        cliente.setNombre("Pedro Nuevo");

        when(service.update(any(), any())).thenReturn(cliente);

        mockMvc.perform(put("/api/v1/clientes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/clientes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
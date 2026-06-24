package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.controller.ClienteController;
import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.security.JwtAuthFilter;
import cl.duoc.vetcontrol.cliente.security.SecurityConfig;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
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
    private ClienteService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeRealizarGet()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/clientes")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeRealizarGet()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/clientes")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioNoAutorizadoNoPuedeRealizarGet()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/clientes")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeCrearCliente()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        crearRequest()
                                                )
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeCrearCliente()
            throws Exception {

        when(service.create(any(ClienteRequest.class)))
                .thenReturn(crearCliente());

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        crearRequest()
                                                )
                                )
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrearCliente()
            throws Exception {

        when(service.create(any(ClienteRequest.class)))
                .thenReturn(crearCliente());

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        crearRequest()
                                                )
                                )
                )
                .andExpect(status().isCreated());
    }

    private ClienteRequest crearRequest() {
        return new ClienteRequest(
                "11111111-1",
                "Joaquín González",
                "+56999999999",
                "joaquin@correo.cl",
                "Recoleta"
        );
    }

    private Cliente crearCliente() {

        Cliente cliente = new Cliente();

        cliente.setId(1L);
        cliente.setRut("11111111-1");
        cliente.setNombre("Joaquín González");
        cliente.setTelefono("+56999999999");
        cliente.setCorreo("joaquin@correo.cl");
        cliente.setDireccion("Recoleta");

        return cliente;
    }
}
package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.controller.InventarioController;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.security.JwtAuthFilter;
import cl.duoc.vetcontrol.inventario.security.SecurityConfig;
import cl.duoc.vetcontrol.inventario.service.InventarioService;
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

@WebMvcTest(InventarioController.class)
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
    private InventarioService service;

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeConsultarInventario()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/inventario")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeConsultarInventario()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/inventario")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioComunNoPuedeConsultarInventario()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/inventario")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrearInventario()
            throws Exception {

        when(service.create(
                any(InventarioRequest.class)
        )).thenReturn(crearItem());

        mockMvc.perform(
                        post("/api/v1/inventario")
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
    void recepcionistaPuedeCrearInventario()
            throws Exception {

        when(service.create(
                any(InventarioRequest.class)
        )).thenReturn(crearItem());

        mockMvc.perform(
                        post("/api/v1/inventario")
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
    void veterinarioNoPuedeCrearInventario()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/inventario")
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
    void recepcionistaPuedeDescontarStock()
            throws Exception {

        when(service.descontarStock(
                100L,
                2
        )).thenReturn(crearItem());

        mockMvc.perform(
                        put(
                                "/api/v1/inventario/productos/100/descontar/2"
                        )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeDescontarStock()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/inventario/productos/100/descontar/2"
                        )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminarInventario()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/v1/inventario/productos/100"
                        )
                )
                .andExpect(status().isNoContent());
    }

    private InventarioRequest crearRequest() {
        return new InventarioRequest(
                100L,
                10,
                3
        );
    }

    private InventarioItem crearItem() {
        InventarioItem item =
                new InventarioItem();

        item.setId(1L);
        item.setProductoId(100L);
        item.setStockActual(10);
        item.setStockMinimo(3);
        item.setActivo(true);

        return item;
    }
}
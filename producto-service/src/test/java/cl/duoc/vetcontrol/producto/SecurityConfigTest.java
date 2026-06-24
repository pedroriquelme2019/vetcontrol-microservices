package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.controller.ProductoController;
import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.security.JwtAuthFilter;
import cl.duoc.vetcontrol.producto.security.SecurityConfig;
import cl.duoc.vetcontrol.producto.service.ProductoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
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
    private ProductoService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeRealizarGet() throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/productos")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeRealizarGet() throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/productos")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioPuedeRealizarGet() throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/productos")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioSinPermisoNoPuedeRealizarGet()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/productos")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeCrearProducto()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/productos")
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
    @WithMockUser(roles = "USER")
    void usuarioSinPermisoNoPuedeCrearProducto()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/productos")
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
    void recepcionistaPuedeCrearProducto()
            throws Exception {

        when(service.create(any(ProductoRequest.class)))
                .thenReturn(crearProducto());

        mockMvc.perform(
                        post("/api/v1/productos")
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
    void adminPuedeCrearProducto()
            throws Exception {

        when(service.create(any(ProductoRequest.class)))
                .thenReturn(crearProducto());

        mockMvc.perform(
                        post("/api/v1/productos")
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
    void adminPuedeEliminarProducto()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/productos/1")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeEliminarProducto()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/productos/1")
                )
                .andExpect(status().isForbidden());
    }

    private ProductoRequest crearRequest() {
        return new ProductoRequest(
                "Antiparasitario",
                "Medicamento",
                new BigDecimal("15000.00"),
                false
        );
    }

    private Producto crearProducto() {

        Producto producto = new Producto();

        producto.setId(1L);
        producto.setNombre("Antiparasitario");
        producto.setCategoria("Medicamento");
        producto.setPrecio(
                new BigDecimal("15000.00")
        );
        producto.setRestringido(false);
        producto.setActivo(true);

        return producto;
    }
}
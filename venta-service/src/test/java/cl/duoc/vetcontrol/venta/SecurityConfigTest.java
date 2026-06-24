package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.controller.VentaController;
import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.security.JwtAuthFilter;
import cl.duoc.vetcontrol.venta.security.SecurityConfig;
import cl.duoc.vetcontrol.venta.service.VentaService;
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

@WebMvcTest(VentaController.class)
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
    private VentaService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeConsultarVentas()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeConsultarVentas()
            throws Exception {

        when(service.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeConsultarVentas()
            throws Exception {

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeRegistrarVenta()
            throws Exception {

        when(service.create(any(VentaRequest.class)))
                .thenReturn(crearVenta());

        mockMvc.perform(
                        post("/api/v1/ventas")
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
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaPuedeRegistrarVenta()
            throws Exception {

        when(service.create(any(VentaRequest.class)))
                .thenReturn(crearVenta());

        mockMvc.perform(
                        post("/api/v1/ventas")
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
    void veterinarioNoPuedeRegistrarVenta()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/ventas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }

    private VentaRequest crearRequest() {
        return new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(
                                100L,
                                2
                        )
                )
        );
    }

    private Venta crearVenta() {
        Venta venta = new Venta();

        venta.setId(1L);
        venta.setClienteId(10L);
        venta.setMedioPago("EFECTIVO");
        venta.setTotal(new BigDecimal("5000.00"));
        venta.setEstado(EstadoVenta.REGISTRADA);

        return venta;
    }
}
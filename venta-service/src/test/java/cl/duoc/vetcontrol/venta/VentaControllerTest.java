package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.controller.VentaController;
import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.exception.BusinessException;
import cl.duoc.vetcontrol.venta.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.venta.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.service.VentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VentaService service;

    @Test
    void findAllDebeRetornar200() throws Exception {
        when(service.findAll())
                .thenReturn(List.of(crearVenta()));

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(10))
                .andExpect(jsonPath("$[0].estado")
                        .value("REGISTRADA"));
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {
        when(service.findById(1L))
                .thenReturn(crearVenta());

        mockMvc.perform(get("/api/v1/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByClienteDebeRetornar200() throws Exception {
        when(service.findByCliente(10L))
                .thenReturn(List.of(crearVenta()));

        mockMvc.perform(
                        get("/api/v1/ventas/clientes/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId")
                        .value(10));
    }

    @Test
    void createDebeRetornar201() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(5000.00));
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        VentaRequest request = new VentaRequest(
                null,
                "",
                List.of()
        );

        mockMvc.perform(
                        post("/api/v1/ventas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Error de validación"))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(service);
    }

    @Test
    void detalleInvalidoDebeRetornar400()
            throws Exception {

        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(
                                0L,
                                0
                        )
                )
        );

        mockMvc.perform(
                        post("/api/v1/ventas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void ventaInexistenteDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Venta no encontrada: 99"
                        )
                );

        mockMvc.perform(get("/api/v1/ventas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Venta no encontrada: 99"));
    }

    @Test
    void stockInsuficienteDebeRetornar400()
            throws Exception {

        when(service.create(any(VentaRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "Stock insuficiente para producto 100"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/ventas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Stock insuficiente para producto 100"
                ));
    }

    @Test
    void errorGeneralDebeRetornar500()
            throws Exception {

        when(service.findAll())
                .thenThrow(
                        new RuntimeException(
                                "Error de MySQL"
                        )
                );

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(
                        "Error interno del servidor"
                ));
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
        venta.setFecha(LocalDateTime.now());
        venta.setMedioPago("EFECTIVO");
        venta.setTotal(new BigDecimal("5000.00"));
        venta.setEstado(EstadoVenta.REGISTRADA);

        return venta;
    }
}
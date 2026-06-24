package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.controller.ProductoController;
import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.exception.BusinessException;
import cl.duoc.vetcontrol.producto.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.service.ProductoService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService service;

    @Test
    void findAllDebeRetornar200() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(crearProducto()));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(1))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Antiparasitario"));

        verify(service).findAll();
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {

        when(service.findById(1L))
                .thenReturn(crearProducto());

        mockMvc.perform(
                        get("/api/v1/productos/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoria")
                        .value("Medicamento"));
    }

    @Test
    void findByCategoriaDebeRetornar200() throws Exception {

        when(service.byCategoria("Medicamento"))
                .thenReturn(List.of(crearProducto()));

        mockMvc.perform(
                        get(
                                "/api/v1/productos/categoria/Medicamento"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre")
                        .value("Antiparasitario"));
    }

    @Test
    void searchDebeRetornar200() throws Exception {

        when(service.search("anti"))
                .thenReturn(List.of(crearProducto()));

        mockMvc.perform(
                        get("/api/v1/productos/buscar")
                                .param("nombre", "anti")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre")
                        .value("Antiparasitario"));
    }

    @Test
    void createDebeRetornar201() throws Exception {

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateDebeRetornar200() throws Exception {

        when(service.update(
                eq(1L),
                any(ProductoRequest.class)
        )).thenReturn(crearProducto());

        mockMvc.perform(
                        put("/api/v1/productos/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(
                        delete("/api/v1/productos/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        ProductoRequest request =
                new ProductoRequest(
                        "",
                        "",
                        BigDecimal.ZERO,
                        false
                );

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Error de validación"))
                .andExpect(jsonPath("$.details")
                        .isArray());

        verifyNoInteractions(service);
    }

    @Test
    void findByIdInexistenteDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Producto no encontrado: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/productos/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Producto no encontrado: 99"));
    }

    @Test
    void businessExceptionDebeRetornar400()
            throws Exception {

        when(service.create(any(ProductoRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "Operación no permitida"
                        )
                );

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Operación no permitida"));
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
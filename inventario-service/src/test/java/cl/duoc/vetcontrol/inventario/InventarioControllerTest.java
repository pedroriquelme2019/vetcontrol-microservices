package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.controller.InventarioController;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.dto.InventarioUpdateRequest;
import cl.duoc.vetcontrol.inventario.exception.BusinessException;
import cl.duoc.vetcontrol.inventario.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.inventario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioService service;

    @Test
    void findAllDebeRetornar200() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(crearItem()));

        mockMvc.perform(
                        get("/api/v1/inventario")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productoId").value(100))
                .andExpect(jsonPath("$[0].stockActual").value(10));
    }

    @Test
    void findByProductoDebeRetornar200() throws Exception {

        when(service.findByProductoId(100L))
                .thenReturn(crearItem());

        mockMvc.perform(
                        get("/api/v1/inventario/productos/100")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(100));
    }

    @Test
    void createDebeRetornar201() throws Exception {

        when(service.create(any(InventarioRequest.class)))
                .thenReturn(crearItem());

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateDebeRetornar200() throws Exception {

        InventarioItem actualizado =
                crearItem();

        actualizado.setStockActual(25);
        actualizado.setStockMinimo(5);

        when(service.update(
                eq(100L),
                any(InventarioUpdateRequest.class)
        )).thenReturn(actualizado);

        InventarioUpdateRequest request =
                new InventarioUpdateRequest(
                        25,
                        5
                );

        mockMvc.perform(
                        put("/api/v1/inventario/productos/100")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(25))
                .andExpect(jsonPath("$.stockMinimo").value(5));
    }

    @Test
    void validarStockDebeRetornarTrue() throws Exception {

        when(service.validarStock(
                100L,
                5
        )).thenReturn(true);

        mockMvc.perform(
                        get(
                                "/api/v1/inventario/productos/100/validar/5"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void descontarStockDebeRetornarInventarioActualizado()
            throws Exception {

        InventarioItem item = crearItem();
        item.setStockActual(6);

        when(service.descontarStock(
                100L,
                4
        )).thenReturn(item);

        mockMvc.perform(
                        put(
                                "/api/v1/inventario/productos/100/descontar/4"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(6));
    }

    @Test
    void reponerStockDebeRetornarInventarioActualizado()
            throws Exception {

        InventarioItem item = crearItem();
        item.setStockActual(15);

        when(service.reponerStock(
                100L,
                5
        )).thenReturn(item);

        mockMvc.perform(
                        put(
                                "/api/v1/inventario/productos/100/reponer/5"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(15));
    }

    @Test
    void bajoStockDebeRetornar200() throws Exception {

        InventarioItem item = crearItem();
        item.setStockActual(2);

        when(service.bajoStock())
                .thenReturn(List.of(item));

        mockMvc.perform(
                        get("/api/v1/inventario/bajo-stock")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockActual").value(2));
    }

    @Test
    void deleteDebeRetornar204() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/v1/inventario/productos/100"
                        )
                )
                .andExpect(status().isNoContent());

        verify(service).delete(100L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        InventarioRequest request =
                new InventarioRequest(
                        null,
                        -1,
                        -1
                );

        mockMvc.perform(
                        post("/api/v1/inventario")
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Error de validación"))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(service);
    }

    @Test
    void updateConDatosInvalidosDebeRetornar400()
            throws Exception {

        InventarioUpdateRequest request =
                new InventarioUpdateRequest(
                        -1,
                        -1
                );

        mockMvc.perform(
                        put("/api/v1/inventario/productos/100")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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
    void productoSinInventarioDebeRetornar404()
            throws Exception {

        when(service.findByProductoId(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Inventario no encontrado para producto: 999"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/inventario/productos/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Inventario no encontrado para producto: 999"
                ));
    }

    @Test
    void stockInsuficienteDebeRetornar400()
            throws Exception {

        when(service.descontarStock(
                100L,
                50
        )).thenThrow(
                new BusinessException(
                        "Stock insuficiente para producto 100"
                )
        );

        mockMvc.perform(
                        put(
                                "/api/v1/inventario/productos/100/descontar/50"
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
                                "Error MySQL"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/inventario")
                )
                .andExpect(
                        status().isInternalServerError()
                )
                .andExpect(jsonPath("$.message").value(
                        "Error interno del servidor"
                ));
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
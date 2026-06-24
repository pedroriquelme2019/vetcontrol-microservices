package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.controller.ClienteController;
import cl.duoc.vetcontrol.cliente.dto.ClienteRequest;
import cl.duoc.vetcontrol.cliente.exception.BusinessException;
import cl.duoc.vetcontrol.cliente.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.cliente.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService service;

    @Test
    void findAllDebeRetornar200YLista() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(crearCliente()));

        mockMvc.perform(
                        get("/api/v1/clientes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rut")
                        .value("11111111-1"))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Joaquín González"))
                .andExpect(jsonPath("$[0].activo")
                        .value(true));

        verify(service).findAll();
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {

        when(service.findById(1L))
                .thenReturn(crearCliente());

        mockMvc.perform(
                        get("/api/v1/clientes/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo")
                        .value("joaquin@correo.cl"));

        verify(service).findById(1L);
    }

    @Test
    void searchDebeRetornar200() throws Exception {

        when(service.search("Joaquín"))
                .thenReturn(List.of(crearCliente()));

        mockMvc.perform(
                        get("/api/v1/clientes/buscar")
                                .param(
                                        "nombre",
                                        "Joaquín"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre")
                        .value("Joaquín González"));

        verify(service).search("Joaquín");
    }

    @Test
    void createDebeRetornar201() throws Exception {

        ClienteRequest request = crearRequest();

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
                                                        request
                                                )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre")
                        .value("Joaquín González"));

        verify(service)
                .create(any(ClienteRequest.class));
    }

    @Test
    void updateDebeRetornar200() throws Exception {

        ClienteRequest request = crearRequest();

        when(service.update(
                eq(1L),
                any(ClienteRequest.class)
        )).thenReturn(crearCliente());

        mockMvc.perform(
                        put("/api/v1/clientes/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).update(
                eq(1L),
                any(ClienteRequest.class)
        );
    }

    @Test
    void deleteDebeRetornar204() throws Exception {

        doNothing()
                .when(service)
                .delete(1L);

        mockMvc.perform(
                        delete("/api/v1/clientes/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        ClienteRequest request =
                new ClienteRequest(
                        "",
                        "",
                        "",
                        "correo-invalido",
                        ""
                );

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
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
                                "Cliente no encontrado: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/clientes/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Cliente no encontrado: 99"
                        ));
    }

    @Test
    void createDuplicadoDebeRetornar400()
            throws Exception {

        when(service.create(any(ClienteRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "Ya existe un cliente con ese RUT"
                        )
                );

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Ya existe un cliente con ese RUT"
                        ));
    }

    private ClienteRequest crearRequest() {
        return new ClienteRequest(
                "11111111-1",
                "Joaquín González",
                "+56999999999",
                "joaquin@correo.cl",
                "Recoleta, Santiago"
        );
    }

    private Cliente crearCliente() {

        Cliente cliente = new Cliente();

        cliente.setId(1L);
        cliente.setRut("11111111-1");
        cliente.setNombre("Joaquín González");
        cliente.setTelefono("+56999999999");
        cliente.setCorreo("joaquin@correo.cl");
        cliente.setDireccion("Recoleta, Santiago");
        cliente.setActivo(true);

        return cliente;
    }
}
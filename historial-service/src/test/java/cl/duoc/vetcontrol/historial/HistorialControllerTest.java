package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.controller.HistorialController;
import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.exception.BusinessException;
import cl.duoc.vetcontrol.historial.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistorialController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "security.jwt.secret=vetcontrol-secret-key-2026-vetcontrol-secret-key-2026"
})
class HistorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HistorialService service;

    @Test
    void findAllDebeRetornar200() throws Exception {
        when(service.findAll())
                .thenReturn(List.of(crearHistorial()));

        mockMvc.perform(
                        get("/api/v1/historiales")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].tipo")
                                .value("VACUNA")
                );
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {
        when(service.findById(1L))
                .thenReturn(crearHistorial());

        mockMvc.perform(
                        get("/api/v1/historiales/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.mascotaId")
                                .value(10)
                );
    }

    @Test
    void findByMascotaDebeRetornar200() throws Exception {
        when(service.findByMascota(10L))
                .thenReturn(
                        List.of(crearHistorial())
                );

        mockMvc.perform(
                        get(
                                "/api/v1/historiales/mascotas/10"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].mascotaId")
                                .value(10)
                );
    }

    @Test
    void createDebeRetornar201() throws Exception {
        when(service.create(
                any(HistorialRequest.class)
        )).thenReturn(crearHistorial());

        mockMvc.perform(
                        post("/api/v1/historiales")
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
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.tipo")
                                .value("VACUNA")
                );
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        HistorialRequest request =
                new HistorialRequest(
                        null,
                        LocalDateTime.now().plusDays(1),
                        "",
                        "",
                        -1L
                );

        mockMvc.perform(
                        post("/api/v1/historiales")
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
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Error de validación")
                )
                .andExpect(
                        jsonPath("$.details")
                                .isArray()
                );

        verifyNoInteractions(service);
    }

    @Test
    void historialNoEncontradoDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Historial no encontrado: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/historiales/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Historial no encontrado: 99"
                                )
                );
    }

    @Test
    void referenciaDuplicadaDebeRetornar400()
            throws Exception {

        when(service.create(
                any(HistorialRequest.class)
        )).thenThrow(
                new BusinessException(
                        "Referencia duplicada"
                )
        );

        mockMvc.perform(
                        post("/api/v1/historiales")
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
                .andExpect(
                        jsonPath("$.message")
                                .value("Referencia duplicada")
                );
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
                        get("/api/v1/historiales")
                )
                .andExpect(
                        status()
                                .isInternalServerError()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Error interno del servidor"
                                )
                );
    }

    private HistorialRequest crearRequest() {
        return new HistorialRequest(
                10L,
                LocalDateTime.now().minusHours(1),
                "VACUNA",
                "Vacuna antirrábica aplicada",
                50L
        );
    }

    private HistorialClinico crearHistorial() {
        HistorialClinico historial =
                new HistorialClinico();

        historial.setId(1L);
        historial.setMascotaId(10L);
        historial.setFecha(
                LocalDateTime.now().minusHours(1)
        );
        historial.setTipo("VACUNA");
        historial.setDetalle(
                "Vacuna antirrábica aplicada"
        );
        historial.setReferenciaExternaId(50L);

        return historial;
    }
}
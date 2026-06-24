package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.controller.AtencionController;
import cl.duoc.vetcontrol.atencion.dto.AtencionRequest;
import cl.duoc.vetcontrol.atencion.exception.BusinessException;
import cl.duoc.vetcontrol.atencion.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.atencion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.atencion.model.Atencion;
import cl.duoc.vetcontrol.atencion.service.AtencionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtencionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AtencionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AtencionService service;

    @Test
    void findAllDebeRetornar200() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(crearAtencion()));

        mockMvc.perform(get("/api/v1/atenciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].diagnostico")
                        .value("Dermatitis"));

        verify(service).findAll();
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {

        when(service.findById(1L))
                .thenReturn(crearAtencion());

        mockMvc.perform(
                        get("/api/v1/atenciones/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mascotaId")
                        .value(10));

        verify(service).findById(1L);
    }

    @Test
    void findByMascotaDebeRetornar200() throws Exception {

        when(service.byMascota(10L))
                .thenReturn(List.of(crearAtencion()));

        mockMvc.perform(
                        get("/api/v1/atenciones/mascotas/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mascotaId")
                        .value(10));

        verify(service).byMascota(10L);
    }

    @Test
    void findByVeterinarioDebeRetornar200()
            throws Exception {

        when(service.byVeterinario(20L))
                .thenReturn(List.of(crearAtencion()));

        mockMvc.perform(
                        get("/api/v1/atenciones/veterinarios/20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].veterinarioId")
                        .value(20));

        verify(service).byVeterinario(20L);
    }

    @Test
    void createDebeRetornar201() throws Exception {

        when(service.create(any(AtencionRequest.class)))
                .thenReturn(crearAtencion());

        mockMvc.perform(
                        post("/api/v1/atenciones")
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.activo").value(true));

        verify(service).create(
                any(AtencionRequest.class)
        );
    }

    @Test
    void updateDebeRetornar200() throws Exception {

        when(service.update(
                eq(1L),
                any(AtencionRequest.class)
        )).thenReturn(crearAtencion());

        mockMvc.perform(
                        put("/api/v1/atenciones/1")
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

        verify(service).update(
                eq(1L),
                any(AtencionRequest.class)
        );
    }

    @Test
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(
                        delete("/api/v1/atenciones/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        AtencionRequest request =
                new AtencionRequest(
                        null,
                        0L,
                        -1L,
                        LocalDateTime.now().plusDays(1),
                        "",
                        "",
                        "a".repeat(501)
                );

        mockMvc.perform(
                        post("/api/v1/atenciones")
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
    void findByIdInexistenteDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Atención no encontrada: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/atenciones/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Atención no encontrada: 99"));
    }

    @Test
    void citaDuplicadaDebeRetornar400()
            throws Exception {

        when(service.create(any(AtencionRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "Ya existe una atención registrada para la cita: 5"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/atenciones")
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
                .andExpect(jsonPath("$.message").value(
                        "Ya existe una atención registrada para la cita: 5"
                ));
    }

    @Test
    void errorGeneralDebeRetornar500()
            throws Exception {

        when(service.findAll())
                .thenThrow(
                        new RuntimeException("Error de base de datos")
                );

        mockMvc.perform(get("/api/v1/atenciones"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Error interno del servidor"));
    }

    private AtencionRequest crearRequest() {
        return new AtencionRequest(
                5L,
                10L,
                20L,
                LocalDateTime.now().minusHours(1),
                "Dermatitis",
                "Antihistamínico",
                "Control en siete días"
        );
    }

    private Atencion crearAtencion() {

        Atencion atencion = new Atencion();

        atencion.setId(1L);
        atencion.setCitaId(5L);
        atencion.setMascotaId(10L);
        atencion.setVeterinarioId(20L);
        atencion.setFechaAtencion(
                LocalDateTime.now().minusHours(1)
        );
        atencion.setDiagnostico("Dermatitis");
        atencion.setTratamiento("Antihistamínico");
        atencion.setObservaciones(
                "Control en siete días"
        );
        atencion.setActivo(true);

        return atencion;
    }
}
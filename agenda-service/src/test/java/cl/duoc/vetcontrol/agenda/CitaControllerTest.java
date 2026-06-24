package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.controller.CitaController;
import cl.duoc.vetcontrol.agenda.dto.CitaRequest;
import cl.duoc.vetcontrol.agenda.exception.BusinessException;
import cl.duoc.vetcontrol.agenda.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.agenda.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.agenda.model.Cita;
import cl.duoc.vetcontrol.agenda.model.EstadoCita;
import cl.duoc.vetcontrol.agenda.service.CitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CitaService service;

    @Test
    void findAllDebeRetornar200() throws Exception {
        when(service.findAll())
                .thenReturn(List.of(crearCita()));

        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado")
                        .value("PROGRAMADA"));
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {
        when(service.findById(1L))
                .thenReturn(crearCita());

        mockMvc.perform(get("/api/v1/citas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByFechaDebeRetornar200() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        when(service.byFecha(fecha))
                .thenReturn(List.of(crearCita()));

        mockMvc.perform(
                        get("/api/v1/citas/fecha/{fecha}", fecha)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mascotaId")
                        .value(10));
    }

    @Test
    void findByMascotaDebeRetornar200() throws Exception {
        when(service.byMascota(10L))
                .thenReturn(List.of(crearCita()));

        mockMvc.perform(
                        get("/api/v1/citas/mascotas/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mascotaId")
                        .value(10));
    }

    @Test
    void findByVeterinarioDebeRetornar200() throws Exception {
        when(service.byVeterinario(20L))
                .thenReturn(List.of(crearCita()));

        mockMvc.perform(
                        get("/api/v1/citas/veterinarios/20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].veterinarioId")
                        .value(20));
    }

    @Test
    void createDebeRetornar201() throws Exception {
        when(service.create(any(CitaRequest.class)))
                .thenReturn(crearCita());

        mockMvc.perform(
                        post("/api/v1/citas")
                                .contentType(MediaType.APPLICATION_JSON)
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
                any(CitaRequest.class)
        )).thenReturn(crearCita());

        mockMvc.perform(
                        put("/api/v1/citas/1")
                                .contentType(MediaType.APPLICATION_JSON)
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
        mockMvc.perform(delete("/api/v1/citas/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        CitaRequest request = new CitaRequest(
                null,
                0L,
                LocalDate.now().minusDays(1),
                null,
                ""
        );

        mockMvc.perform(
                        post("/api/v1/citas")
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
    void findByIdInexistenteDebeRetornar404()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Cita no encontrada: 99"
                        )
                );

        mockMvc.perform(get("/api/v1/citas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cita no encontrada: 99"));
    }

    @Test
    void horarioOcupadoDebeRetornar400()
            throws Exception {

        when(service.create(any(CitaRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "El veterinario ya tiene una cita en ese bloque horario"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/citas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                crearRequest()
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "El veterinario ya tiene una cita en ese bloque horario"
                ));
    }

    @Test
    void errorGeneralDebeRetornar500()
            throws Exception {

        when(service.findAll())
                .thenThrow(
                        new RuntimeException(
                                "Error interno de MySQL"
                        )
                );

        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Error interno del servidor"));
    }

    private CitaRequest crearRequest() {
        return new CitaRequest(
                10L,
                20L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Control veterinario"
        );
    }

    private Cita crearCita() {
        Cita cita = new Cita();

        cita.setId(1L);
        cita.setMascotaId(10L);
        cita.setVeterinarioId(20L);
        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        cita.setMotivo("Control veterinario");
        cita.setEstado(EstadoCita.PROGRAMADA);

        return cita;
    }
}
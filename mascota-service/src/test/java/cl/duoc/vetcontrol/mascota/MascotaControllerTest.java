package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.controller.MascotaController;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.exception.BusinessException;
import cl.duoc.vetcontrol.mascota.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
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

@WebMvcTest(MascotaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MascotaService service;

    @Test
    void findAllDebeRetornar200YLista() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(crearMascota()));

        mockMvc.perform(get("/api/v1/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Firulais"))
                .andExpect(jsonPath("$[0].activo")
                        .value(true));

        verify(service).findAll();
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {

        when(service.findById(1L))
                .thenReturn(crearMascota());

        mockMvc.perform(get("/api/v1/mascotas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.especie")
                        .value("Perro"));

        verify(service).findById(1L);
    }

    @Test
    void findByClienteDebeRetornar200() throws Exception {

        when(service.findByCliente(10L))
                .thenReturn(List.of(crearMascota()));

        mockMvc.perform(
                        get("/api/v1/mascotas/cliente/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre")
                        .value("Firulais"));

        verify(service).findByCliente(10L);
    }

    @Test
    void searchDebeRetornar200() throws Exception {

        when(service.search("fir"))
                .thenReturn(List.of(crearMascota()));

        mockMvc.perform(
                        get("/api/v1/mascotas/buscar")
                                .param("nombre", "fir")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre")
                        .value("Firulais"));

        verify(service).search("fir");
    }

    @Test
    void createDebeRetornar201() throws Exception {

        when(service.create(any(MascotaRequest.class)))
                .thenReturn(crearMascota());

        mockMvc.perform(
                        post("/api/v1/mascotas")
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
                .andExpect(jsonPath("$.nombre")
                        .value("Firulais"));

        verify(service)
                .create(any(MascotaRequest.class));
    }

    @Test
    void updateDebeRetornar200() throws Exception {

        when(service.update(
                eq(1L),
                any(MascotaRequest.class)
        )).thenReturn(crearMascota());

        mockMvc.perform(
                        put("/api/v1/mascotas/1")
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
                any(MascotaRequest.class)
        );
    }

    @Test
    void deleteDebeRetornar204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(
                        delete("/api/v1/mascotas/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        MascotaRequest request = new MascotaRequest(
                null,
                "",
                "",
                null,
                -1,
                null,
                -2.0,
                null
        );

        mockMvc.perform(
                        post("/api/v1/mascotas")
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
                                "Mascota no encontrada: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/mascotas/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Mascota no encontrada: 99"));
    }

    @Test
    void createConClienteInvalidoDebeRetornar400()
            throws Exception {

        when(service.create(any(MascotaRequest.class)))
                .thenThrow(
                        new BusinessException(
                                "El cliente dueño no existe o no está disponible"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/mascotas")
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
                        "El cliente dueño no existe o no está disponible"
                ));
    }

    private MascotaRequest crearRequest() {
        return new MascotaRequest(
                1L,
                "Firulais",
                "Perro",
                "Labrador",
                5,
                "Macho",
                18.5,
                "CHIP-001"
        );
    }

    private Mascota crearMascota() {

        Mascota mascota = new Mascota();

        mascota.setId(1L);
        mascota.setClienteId(1L);
        mascota.setNombre("Firulais");
        mascota.setEspecie("Perro");
        mascota.setRaza("Labrador");
        mascota.setEdad(5);
        mascota.setSexo("Macho");
        mascota.setPeso(18.5);
        mascota.setMicrochip("CHIP-001");
        mascota.setActivo(true);

        return mascota;
    }
}
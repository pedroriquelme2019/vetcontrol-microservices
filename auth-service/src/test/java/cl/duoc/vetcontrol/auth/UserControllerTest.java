package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.controller.UserController;
import cl.duoc.vetcontrol.auth.dto.UserRequest;
import cl.duoc.vetcontrol.auth.dto.UserResponse;
import cl.duoc.vetcontrol.auth.dto.UserUpdateRequest;
import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.auth.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.service.AuthService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void findAllDebeRetornar200() throws Exception {
        when(authService.findAll())
                .thenReturn(List.of(crearResponse()));

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].username")
                                .value("admin")
                )
                .andExpect(
                        jsonPath("$[0].role")
                                .value("ADMIN")
                )
                .andExpect(
                        jsonPath("$[0].passwordHash")
                                .doesNotExist()
                );
    }

    @Test
    void findByIdDebeRetornar200() throws Exception {
        when(authService.findById(1L))
                .thenReturn(crearResponse());

        mockMvc.perform(
                        get("/api/v1/users/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value("admin@vetcontrol.cl")
                );
    }

    @Test
    void createDebeRetornar201() throws Exception {
        when(authService.create(
                any(UserRequest.class)
        )).thenReturn(crearResponse());

        mockMvc.perform(
                        post("/api/v1/users")
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
                );
    }

    @Test
    void createConDatosInvalidosDebeRetornar400()
            throws Exception {

        UserRequest request = new UserRequest(
                "",
                "correo-invalido",
                "123",
                null
        );

        mockMvc.perform(
                        post("/api/v1/users")
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
                        jsonPath("$.message")
                                .value("Error de validación")
                );

        verifyNoInteractions(authService);
    }

    @Test
    void createConRolInvalidoDebeRetornar400()
            throws Exception {

        String json = """
                {
                  "username": "nuevo",
                  "email": "nuevo@vetcontrol.cl",
                  "password": "password123",
                  "role": "SUPERADMIN"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Cuerpo de solicitud inválido"
                                )
                );
    }

    @Test
    void updateDebeRetornar200() throws Exception {
        UserResponse actualizado =
                new UserResponse(
                        1L,
                        "admin",
                        "nuevo@vetcontrol.cl",
                        Role.RECEPCIONISTA,
                        false,
                        LocalDateTime.now()
                );

        when(authService.update(
                eq(1L),
                any(UserUpdateRequest.class)
        )).thenReturn(actualizado);

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "nuevo@vetcontrol.cl",
                        "nuevaClave123",
                        Role.RECEPCIONISTA,
                        false
                );

        mockMvc.perform(
                        put("/api/v1/users/1")
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
                .andExpect(
                        jsonPath("$.role")
                                .value("RECEPCIONISTA")
                )
                .andExpect(
                        jsonPath("$.enabled")
                                .value(false)
                );
    }

    @Test
    void cambiarEstadoDebeRetornar200()
            throws Exception {

        UserResponse deshabilitado =
                new UserResponse(
                        1L,
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN,
                        false,
                        LocalDateTime.now()
                );

        when(authService.cambiarEstado(
                1L,
                false
        )).thenReturn(deshabilitado);

        mockMvc.perform(
                        put(
                                "/api/v1/users/1/estado"
                        )
                                .param(
                                        "enabled",
                                        "false"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.enabled")
                                .value(false)
                );
    }

    @Test
    void disableDebeRetornar204() throws Exception {
        doNothing()
                .when(authService)
                .disable(1L);

        mockMvc.perform(
                        delete("/api/v1/users/1")
                )
                .andExpect(status().isNoContent());

        verify(authService).disable(1L);
    }

    @Test
    void usuarioNoEncontradoDebeRetornar404()
            throws Exception {

        when(authService.findById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Usuario no encontrado: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/users/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Usuario no encontrado: 99"
                                )
                );
    }

    @Test
    void usernameDuplicadoDebeRetornar400()
            throws Exception {

        when(authService.create(
                any(UserRequest.class)
        )).thenThrow(
                new BusinessException(
                        "El username ya existe"
                )
        );

        mockMvc.perform(
                        post("/api/v1/users")
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
                                .value(
                                        "El username ya existe"
                                )
                );
    }

    private UserRequest crearRequest() {
        return new UserRequest(
                "admin",
                "admin@vetcontrol.cl",
                "password123",
                Role.ADMIN
        );
    }

    private UserResponse crearResponse() {
        return new UserResponse(
                1L,
                "admin",
                "admin@vetcontrol.cl",
                Role.ADMIN,
                true,
                LocalDateTime.of(
                        2026,
                        6,
                        1,
                        10,
                        0
                )
        );
    }
}
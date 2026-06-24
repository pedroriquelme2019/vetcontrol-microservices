package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.controller.AuthController;
import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import cl.duoc.vetcontrol.auth.exception.GlobalExceptionHandler;
import cl.duoc.vetcontrol.auth.exception.InvalidCredentialsException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void loginDebeRetornar200YToken() throws Exception {
        when(authService.login(
                any(LoginRequest.class)
        )).thenReturn(
                new AuthResponse(
                        "jwt-token",
                        "Bearer",
                        "admin",
                        "ADMIN"
                )
        );

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "admin123"
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
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
                        jsonPath("$.token")
                                .value("jwt-token")
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("admin")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("ADMIN")
                );
    }

    @Test
    void loginConDatosVaciosDebeRetornar400()
            throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "",
                        ""
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
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
                )
                .andExpect(
                        jsonPath("$.details")
                                .isArray()
                );

        verifyNoInteractions(authService);
    }

    @Test
    void credencialesInvalidasDebenRetornar401()
            throws Exception {

        when(authService.login(
                any(LoginRequest.class)
        )).thenThrow(
                new InvalidCredentialsException(
                        "Credenciales inválidas"
                )
        );

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "incorrecta"
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Credenciales inválidas")
                );
    }
}
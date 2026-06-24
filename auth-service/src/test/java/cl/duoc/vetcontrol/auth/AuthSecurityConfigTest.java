package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.controller.AuthController;
import cl.duoc.vetcontrol.auth.controller.UserController;
import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import cl.duoc.vetcontrol.auth.dto.UserResponse;
import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.security.AuthSecurityConfig;
import cl.duoc.vetcontrol.auth.security.JwtAuthFilter;
import cl.duoc.vetcontrol.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthController.class,
        UserController.class
})
@Import({
        AuthSecurityConfig.class,
        JwtAuthFilter.class
})
@TestPropertySource(properties = {
        "security.jwt.secret=vetcontrol-secret-key-2026-vetcontrol-secret-key-2026"
})
class AuthSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthService authService;

    @Test
    void loginDebeSerPublico() throws Exception {
        when(authService.login(
                any(LoginRequest.class)
        )).thenReturn(
                new AuthResponse(
                        "token",
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
                .andExpect(status().isOk());
    }

    @Test
    void usuariosSinAutenticacionNoPuedenConsultar()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeConsultarUsuarios()
            throws Exception {

        when(authService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeConsultarUsuarios()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNoPuedeConsultarUsuarios()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminarUsuario()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/users/1")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void passwordEncoderDebeUsarBCrypt() {
        String hash =
                passwordEncoder.encode(
                        "password123"
                );

        assertNotEquals(
                "password123",
                hash
        );

        assertTrue(
                passwordEncoder.matches(
                        "password123",
                        hash
                )
        );

        assertFalse(
                passwordEncoder.matches(
                        "incorrecta",
                        hash
                )
        );
    }
}
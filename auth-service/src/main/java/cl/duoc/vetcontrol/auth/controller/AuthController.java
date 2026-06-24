package cl.duoc.vetcontrol.auth.controller;

import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import cl.duoc.vetcontrol.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}
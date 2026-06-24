package cl.duoc.vetcontrol.auth.controller;

import cl.duoc.vetcontrol.auth.dto.UserRequest;
import cl.duoc.vetcontrol.auth.dto.UserResponse;
import cl.duoc.vetcontrol.auth.dto.UserUpdateRequest;
import cl.duoc.vetcontrol.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    public UserController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(
                authService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                authService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid
            @RequestBody
            UserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        authService.create(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                authService.update(
                        id,
                        request
                )
        );
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<UserResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(
                authService.cambiarEstado(
                        id,
                        enabled
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(
            @PathVariable Long id
    ) {
        authService.disable(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
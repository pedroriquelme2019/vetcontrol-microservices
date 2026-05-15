package cl.duoc.vetcontrol.auth.controller;

import cl.duoc.vetcontrol.auth.dto.UserRequest;
import cl.duoc.vetcontrol.auth.model.UserAccount;
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
    public UserController(AuthService authService) { this.authService = authService; }

    @GetMapping
    public ResponseEntity<List<UserAccount>> findAll() { return ResponseEntity.ok(authService.findAll()); }

    @PostMapping
    public ResponseEntity<UserAccount> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.create(request));
    }
}

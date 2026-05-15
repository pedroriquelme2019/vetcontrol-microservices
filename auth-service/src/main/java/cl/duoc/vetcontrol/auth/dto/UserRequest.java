package cl.duoc.vetcontrol.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
        @Pattern(regexp = "ADMIN|RECEPCIONISTA|VETERINARIO", message = "Rol permitido: ADMIN, RECEPCIONISTA o VETERINARIO") String role
) {}

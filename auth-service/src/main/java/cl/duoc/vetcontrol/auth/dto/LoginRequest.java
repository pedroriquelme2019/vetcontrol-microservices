package cl.duoc.vetcontrol.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(
                message = "El usuario o correo es obligatorio"
        )
        @Size(
                max = 120,
                message = "El usuario o correo no puede superar los 120 caracteres"
        )
        String username,

        @NotBlank(
                message = "La contraseña es obligatoria"
        )
        @Size(
                max = 72,
                message = "La contraseña no puede superar los 72 caracteres"
        )
        String password

) {
}
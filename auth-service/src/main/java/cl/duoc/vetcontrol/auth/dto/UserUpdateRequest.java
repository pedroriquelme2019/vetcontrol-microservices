package cl.duoc.vetcontrol.auth.dto;

import cl.duoc.vetcontrol.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Email(
                message = "El correo no tiene un formato válido"
        )
        @Size(
                max = 120,
                message = "El correo no puede superar los 120 caracteres"
        )
        String email,

        @Size(
                min = 8,
                max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres"
        )
        String password,

        Role role,

        Boolean enabled

) {
}
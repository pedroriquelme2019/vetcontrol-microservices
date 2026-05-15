package cl.duoc.vetcontrol.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 12) String rut,
        @NotBlank String nombre,
        @NotBlank String telefono,
        @Email @NotBlank String correo,
        @NotBlank String direccion
) {}

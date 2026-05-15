package cl.duoc.vetcontrol.veterinario.dto;
import jakarta.validation.constraints.*;
public record VeterinarioRequest(@NotBlank String rut, @NotBlank String nombre, @NotBlank String especialidad, @Email @NotBlank String correo) {}

package cl.duoc.vetcontrol.mascota.dto;
import jakarta.validation.constraints.*;
public record MascotaRequest(
        @NotNull Long clienteId,
        @NotBlank String nombre,
        @NotBlank String especie,
        String raza,
        @Min(0) Integer edad,
        String sexo,
        @PositiveOrZero Double peso,
        String microchip
) {}

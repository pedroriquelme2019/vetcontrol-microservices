package cl.duoc.vetcontrol.atencion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AtencionRequest(

        @NotNull
        @Positive
        Long citaId,

        @NotNull
        @Positive
        Long mascotaId,

        @NotNull
        @Positive
        Long veterinarioId,

        @NotNull
        @PastOrPresent
        LocalDateTime fechaAtencion,

        @NotBlank
        @Size(max = 300)
        String diagnostico,

        @NotBlank
        @Size(max = 300)
        String tratamiento,

        @Size(max = 500)
        String observaciones

) {
}
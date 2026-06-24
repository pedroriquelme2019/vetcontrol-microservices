package cl.duoc.vetcontrol.agenda.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(

        @NotNull
        @Positive
        Long mascotaId,

        @NotNull
        @Positive
        Long veterinarioId,

        @NotNull
        @FutureOrPresent
        LocalDate fecha,

        @NotNull
        LocalTime hora,

        @NotBlank
        @Size(max = 160)
        String motivo

) {
}
package cl.duoc.vetcontrol.agenda.dto;
import jakarta.validation.constraints.*;
import java.time.*;
public record CitaRequest(@NotNull Long mascotaId, @NotNull Long veterinarioId, @NotNull LocalDate fecha, @NotNull LocalTime hora, @NotBlank String motivo) {}

package cl.duoc.vetcontrol.atencion.dto;
import jakarta.validation.constraints.*; import java.time.LocalDateTime;
public record AtencionRequest(@NotNull Long citaId, @NotNull Long mascotaId, @NotNull Long veterinarioId, @NotNull LocalDateTime fechaAtencion, @NotBlank String diagnostico, @NotBlank String tratamiento, String observaciones) {}

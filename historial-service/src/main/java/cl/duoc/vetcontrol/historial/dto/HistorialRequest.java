package cl.duoc.vetcontrol.historial.dto;
import jakarta.validation.constraints.*; import java.time.LocalDateTime;
public record HistorialRequest(@NotNull Long mascotaId, @NotNull LocalDateTime fecha, @NotBlank String tipo, @NotBlank String detalle, Long referenciaExternaId) {}

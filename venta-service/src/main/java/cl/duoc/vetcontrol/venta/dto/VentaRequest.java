package cl.duoc.vetcontrol.venta.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.List;
public record VentaRequest(@NotNull Long clienteId, @NotBlank String medioPago, @NotEmpty List<@Valid DetalleVentaRequest> detalles) {}

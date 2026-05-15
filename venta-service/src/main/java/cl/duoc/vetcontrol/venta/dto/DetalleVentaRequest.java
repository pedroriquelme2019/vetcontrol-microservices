package cl.duoc.vetcontrol.venta.dto;
import jakarta.validation.constraints.*;
public record DetalleVentaRequest(@NotNull Long productoId, @NotNull @Positive Integer cantidad) {}

package cl.duoc.vetcontrol.inventario.dto;
import jakarta.validation.constraints.*;
public record InventarioRequest(@NotNull Long productoId, @NotNull @PositiveOrZero Integer stockActual, @NotNull @PositiveOrZero Integer stockMinimo) {}

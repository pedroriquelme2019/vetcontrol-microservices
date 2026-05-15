package cl.duoc.vetcontrol.producto.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductoRequest(@NotBlank String nombre, @NotBlank String categoria, @NotNull @Positive BigDecimal precio, boolean restringido) {}

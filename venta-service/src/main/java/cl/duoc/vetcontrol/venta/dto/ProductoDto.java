package cl.duoc.vetcontrol.venta.dto;
import java.math.BigDecimal;
public record ProductoDto(Long id, String nombre, String categoria, BigDecimal precio, boolean restringido, boolean activo) {}

package cl.duoc.vetcontrol.notificacion.dto;
import jakarta.validation.constraints.*;
public record NotificacionRequest(@NotBlank String tipo, @NotBlank String mensaje) {}

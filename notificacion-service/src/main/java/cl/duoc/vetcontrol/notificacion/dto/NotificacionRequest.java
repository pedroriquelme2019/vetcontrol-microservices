package cl.duoc.vetcontrol.notificacion.dto;

import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacionRequest(

        @NotNull
        TipoNotificacion tipo,

        @NotBlank
        @Size(max = 500)
        String mensaje

) {
}


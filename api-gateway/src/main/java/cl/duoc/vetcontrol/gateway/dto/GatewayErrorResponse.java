package cl.duoc.vetcontrol.gateway.dto;

import java.time.LocalDateTime;

public record GatewayErrorResponse(

        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path

) {
}
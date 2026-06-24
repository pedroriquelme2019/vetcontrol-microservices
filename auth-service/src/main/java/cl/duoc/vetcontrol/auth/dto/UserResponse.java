package cl.duoc.vetcontrol.auth.dto;

import cl.duoc.vetcontrol.auth.model.Role;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String username,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt

) {
}
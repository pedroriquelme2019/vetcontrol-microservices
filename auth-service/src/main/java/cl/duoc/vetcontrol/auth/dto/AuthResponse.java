package cl.duoc.vetcontrol.auth.dto;

public record AuthResponse(String token, String tokenType, String username, String role) {}

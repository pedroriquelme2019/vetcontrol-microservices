package cl.duoc.vetcontrol.gateway.dto;

import java.util.Objects;

public final class JwtUserData {

    private final String username;
    private final String role;
    private final Long userId;

    public JwtUserData(
            String username,
            String role,
            Long userId
    ) {
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof JwtUserData that)) {
            return false;
        }

        return Objects.equals(username, that.username)
                && Objects.equals(role, that.role)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                username,
                role,
                userId
        );
    }

    @Override
    public String toString() {
        return "JwtUserData{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", userId=" + userId +
                '}';
    }
}
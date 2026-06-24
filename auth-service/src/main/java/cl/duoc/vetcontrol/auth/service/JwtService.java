package cl.duoc.vetcontrol.auth.service;

import cl.duoc.vetcontrol.auth.model.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-hours:8}")
    private long expirationHours;

    public String generateToken(
            UserAccount user
    ) {
        Instant now =
                Instant.now();

        Instant expiration =
                now.plus(
                        expirationHours,
                        ChronoUnit.HOURS
                );

        return Jwts.builder()
                .setSubject(
                        user.getUsername()
                )
                .claim(
                        "role",
                        user.getRole().name()
                )
                .claim(
                        "userId",
                        user.getId()
                )
                .setIssuedAt(
                        Date.from(now)
                )
                .setExpiration(
                        Date.from(expiration)
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                secret.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}
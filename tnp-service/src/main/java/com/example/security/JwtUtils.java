package com.example.security;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.types.SubscriptionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public static final String SECRET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long ACCESS_EXPIRATION_TIME = 900000; // 15 min
    private final long REFRESH_EXPIRATION_TIME = 604800000; // 7 days

    public String generateAccessToken(User user, List<SubscriptionType> subscriptions) {
        return generateToken(user, subscriptions, ACCESS_EXPIRATION_TIME);
    }

    public String generateRefreshToken(User user, List<SubscriptionType> subscriptions) {
        return generateToken(user, subscriptions, REFRESH_EXPIRATION_TIME);
    }

    private String generateToken(
            User user, List<SubscriptionType> subscriptions, long expirationTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId()) // ✅ Add this line
                .claim("firstName", user.getFirstName()) // ✅ Added First Name
                .claim("lastName", user.getLastName()) // ✅ Added Last Name
                .claim("email", user.getEmail()) // ✅ Added Email
                .claim("roles", user.getRoles().stream().map(Enum::name).toList()) // ✅ Roles
                .claim(
                        "subscriptions",
                        subscriptions.stream().map(Enum::name).toList()) // ✅ Subscriptions
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        return authHeader.substring(7); // Removes "Bearer "
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

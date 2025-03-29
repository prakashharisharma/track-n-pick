package com.example.security;

import com.example.transactional.model.um.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    public static final String SECRET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long ACCESS_EXPIRATION_TIME = 900000; // 15 min
    private final long REFRESH_EXPIRATION_TIME = 604800000; // 7 days

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_EXPIRATION_TIME);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_EXPIRATION_TIME);
    }

    private String generateToken(User user, long expirationTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("firstName", user.getFirstName())  // ✅ Added First Name
                .claim("lastName", user.getLastName())    // ✅ Added Last Name
                .claim("email", user.getEmail())          // ✅ Added Email
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())  // ✅ Roles
                .claim("subscriptions", user.getSubscriptions().stream().map(Enum::name).toList())  // ✅ Subscriptions
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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
}


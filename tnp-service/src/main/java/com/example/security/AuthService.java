package com.example.security;

import com.example.dto.security.JwtResponse;
import com.example.dto.security.LoginRequest;
import com.example.transactional.exception.InvalidTokenException;
import com.example.transactional.model.um.User;
import com.example.transactional.repo.um.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        // 🔹 Store tokens in Redis with expiry
        redisTemplate.opsForValue().set("TOKEN:" + user.getUsername(), accessToken, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("REFRESH:" + user.getUsername(), refreshToken, 7, TimeUnit.DAYS);

        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refreshAccessToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 🔹 Validate token from Redis
        String storedRefreshToken = redisTemplate.opsForValue().get("REFRESH:" + username);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String newAccessToken = jwtUtils.generateAccessToken(user);
        redisTemplate.opsForValue().set("TOKEN:" + username, newAccessToken, 30, TimeUnit.MINUTES);

        return new JwtResponse(newAccessToken, refreshToken);
    }

    public void logoutUser(String username) {
        // 🔹 Delete tokens from Redis
        redisTemplate.delete("TOKEN:" + username);
        redisTemplate.delete("REFRESH:" + username);
    }
}
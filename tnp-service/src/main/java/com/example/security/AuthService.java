package com.example.security;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.UserRepository;
import com.example.data.transactional.types.SubscriptionType;
import com.example.dto.request.LoginRequest;
import com.example.dto.response.JwtResponse;
import com.example.exception.InvalidTokenException;
import com.example.service.subscription.SubscriptionService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final SubscriptionService subscriptionService;

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;
    @Autowired(required = false)
    private AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // 🔐 Properly authenticate the user
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user =
                userRepository
                        .findByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SubscriptionType> subscriptions = subscriptionService.getActiveSubscriptionTypes(user);

        String accessToken = jwtUtils.generateAccessToken(user, subscriptions);
        String refreshToken = jwtUtils.generateRefreshToken(user, subscriptions);

        // 🔹 Store tokens in Redis with expiry
        redisTemplate
                .opsForValue()
                .set("TOKEN:" + user.getUsername(), accessToken, 30, TimeUnit.MINUTES);
        redisTemplate
                .opsForValue()
                .set("REFRESH:" + user.getUsername(), refreshToken, 7, TimeUnit.DAYS);

        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refreshAccessToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<SubscriptionType> subscriptions = subscriptionService.getActiveSubscriptionTypes(user);
        // 🔹 Validate token from Redis
        String storedRefreshToken = redisTemplate.opsForValue().get("REFRESH:" + username);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String newAccessToken = jwtUtils.generateAccessToken(user, subscriptions);
        redisTemplate.opsForValue().set("TOKEN:" + username, newAccessToken, 30, TimeUnit.MINUTES);

        return new JwtResponse(newAccessToken, refreshToken);
    }

    public void logoutUser(String username) {
        // 🔹 Delete tokens from Redis
        redisTemplate.delete("TOKEN:" + username);
        redisTemplate.delete("REFRESH:" + username);
    }
}

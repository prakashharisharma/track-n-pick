package com.example.web.controller.secured.user;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.UserRepository;
import com.example.security.JwtUtils;
import com.example.service.UserService;
import com.example.web.utils.JsonApiSuccessUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/user/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Operations related to user profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    private final UserRepository userRepository;

    @Operation(
            summary = "Get Dhan Access Token",
            description = "Retrieves the user's Dhan access token",
            responses = {
                @ApiResponse(responseCode = "200", description = "Token retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "Token not found")
            })
    @GetMapping("/dhan-token")
    public ResponseEntity<Map<String, Object>> getDhanAccessToken(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtils.extractUserId(jwtUtils.extractToken(authHeader));
        User user = userService.get(userId);
        return JsonApiSuccessUtil.ok(
                "Dhan Access Token", Map.of("dhanAccessToken", user.getDhanAccessToken()));
    }

    @Operation(
            summary = "Update Dhan Access Token",
            description = "Updates the user's Dhan access token",
            responses = {
                @ApiResponse(responseCode = "200", description = "Token updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid token")
            })
    @PatchMapping("/dhan-token")
    public ResponseEntity<Map<String, Object>> updateDhanAccessToken(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = jwtUtils.extractUserId(jwtUtils.extractToken(authHeader));

        User user = userService.get(userId);

        String newToken = request.get("dhanAccessToken");
        user.setDhanAccessToken(newToken);

        if (newToken == null || newToken.trim().isEmpty()) {
            return JsonApiSuccessUtil.createSuccessResponse(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    Map.of("error", "Invalid token provided"));
        }

        userRepository.save(user);

        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Token updated successfully", newToken);
    }
}

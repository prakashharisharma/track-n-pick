package com.example.web.utils;

import java.util.Collection;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class JsonApiSuccessUtil {

    public static ResponseEntity<Map<String, Object>> createSuccessResponse(
            HttpStatus status, String message, Object data) {
        Map<String, Object> successResponse =
                Map.of(
                        "status",
                        String.valueOf(status.value()),
                        "message",
                        message,
                        "data",
                        (data instanceof Collection<?> || data instanceof Object[])
                                ? Map.of("items", data)
                                : data // Wrap only Collections & Arrays inside "items"
                        );

        return ResponseEntity.status(status).body(successResponse);
    }

    public static ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        return createSuccessResponse(HttpStatus.OK, message, data);
    }

    public static ResponseEntity<Map<String, Object>> created(String message, Object data) {
        return createSuccessResponse(HttpStatus.CREATED, message, data);
    }

    public static ResponseEntity<Map<String, Object>> updated(String message, Object data) {
        return createSuccessResponse(HttpStatus.OK, message, data);
    }

    public static ResponseEntity<Map<String, Object>> deleted(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(
                        Map.of(
                                "status",
                                String.valueOf(HttpStatus.NO_CONTENT.value()),
                                "message",
                                message));
    }
}

package com.example.web.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public class JsonApiErrorUtil {

    public static ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String title, String detail) {
        Map<String, Object> errorResponse = Map.of(
                "status", String.valueOf(status.value()),
                "title", title,
                "errors", List.of(Map.of("detail", detail))
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

}

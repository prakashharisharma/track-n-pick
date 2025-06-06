package com.example.web.exception;

import com.example.exception.InvalidTokenException;
import com.example.web.utils.JsonApiErrorUtil;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(
            InvalidTokenException ex) {
        return JsonApiErrorUtil.createErrorResponse(
                HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UsernameNotFoundException ex) {
        return JsonApiErrorUtil.createErrorResponse(
                HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(RuntimeException ex) {
        return JsonApiErrorUtil.createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong");
    }
}

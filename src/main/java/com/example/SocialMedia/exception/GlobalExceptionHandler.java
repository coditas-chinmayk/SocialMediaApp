package com.example.SocialMedia.exception;

import com.example.SocialMedia.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", e.getMessage());
        data.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>(false, "Invalid argument", data));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleIllegalStateException(IllegalStateException e) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", e.getMessage());
        data.put("status", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto<>(false, "Conflict occurred", data));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleNotFoundException(NoSuchElementException e) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", e.getMessage());
        data.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>(false, "Resource not found", data));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleAccessDeniedException(AccessDeniedException e) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", e.getMessage() + ", You are not authorized to perform this action");
        data.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDto<>(false, "Access denied", data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleAllOther(Exception e) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", e.getMessage());

        data.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(false, "An unexpected error occurred", data));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("errors", errors);
        data.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>(false, "Validation failed", data));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", "wrong username or password");
        data.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponseDto<>(false, "Authentication failed", data));
    }
}
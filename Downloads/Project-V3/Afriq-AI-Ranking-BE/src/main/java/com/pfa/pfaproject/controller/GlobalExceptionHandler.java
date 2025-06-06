package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.exception.CustomException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Afriq-AI Ranking API.
 *
 * Provides centralized exception handling across all controllers,
 * converting various exception types to standardized API responses.
 * This ensures consistent error format and appropriate HTTP status codes
 * for different error scenarios.
 *
 * @since 1.0
 * @version 1.1
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles custom application exceptions.
     *
     * @param e The custom exception
     * @return Standardized error response with the exception's status code
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return createErrorResponse(e.getStatus(), e.getMessage());
    }

    /**
     * Handles bean validation exceptions for @Valid annotated request bodies.
     *
     * @param e The validation exception
     * @return Error response with field-level validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles constraint violation exceptions for @Validated parameters.
     *
     * @param e The constraint violation exception
     * @return Error response with validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        log.error("Constraint violation: {}", errors);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles malformed JSON in request bodies.
     *
     * @param e The exception for malformed JSON
     * @return Error response for invalid request format
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("Malformed JSON request: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request");
    }

    /**
     * Handles type mismatch exceptions for request parameters.
     *
     * @param e The type mismatch exception
     * @return Error response for invalid parameter type
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {} value '{}' is not valid",
                e.getName(), e.getValue());

        String message = String.format("Parameter '%s' with value '%s' is not valid. Expected type: %s",
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName());

        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles missing required parameters.
     *
     * @param e The missing parameter exception
     * @return Error response for missing parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("Missing parameter: {}", e.getParameterName());
        return createErrorResponse(HttpStatus.BAD_REQUEST,
                "Required parameter '" + e.getParameterName() + "' is missing");
    }

    /**
     * Handles access denied exceptions.
     *
     * @param e The access denied exception
     * @return Error response for unauthorized access
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    /**
     * Handles unsupported media type exceptions.
     *
     * @param e The media type not supported exception
     * @return Error response with details about supported media types
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("Unsupported media type: {}", e.getMessage());
        return createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported media type: " + e.getContentType() + ". Supported types: " + e.getSupportedMediaTypes());
    }

    /**
     * Fallback handler for all other unhandled exceptions.
     *
     * @param e The exception
     * @return Generic error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unhandled exception", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /**
     * Creates a standardized error response.
     *
     * @param status HTTP status code
     * @param message Error message
     * @return Response entity with error details
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("code", status.value());

        return ResponseEntity.status(status).body(response);
    }
}

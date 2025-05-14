package com.pfa.pfaproject.controller;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized API responses.
 * 
 * Provides methods for generating consistent response structures for success
 * and error scenarios across the API. This ensures a uniform format for all
 * API responses, making them easier to process for client applications.
 * 
 * @since 1.0
 * @version 1.1
 */
public class ResponseWrapper {
    
    /**
     * Creates a standardized error response.
     * 
     * @param message The error message
     * @return Map containing the error details
     */
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Creates a standardized error response with HTTP status.
     * 
     * @param message The error message
     * @param status The HTTP status
     * @return Map containing the error details
     */
    public static Map<String, Object> error(String message, HttpStatus status) {
        Map<String, Object> response = error(message);
        response.put("code", status.value());
        return response;
    }

    /**
     * Creates a standardized success response.
     * 
     * @param data The data to include in the response
     * @return Map containing the response data
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Creates a standardized success response with pagination information.
     * 
     * @param data The data to include in the response
     * @param page The current page number
     * @param size The page size
     * @param totalElements Total number of elements
     * @param totalPages Total number of pages
     * @return Map containing the response data with pagination metadata
     */
    public static Map<String, Object> successWithPagination(Object data, int page, int size, 
                                                          long totalElements, int totalPages) {
        Map<String, Object> response = success(data);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalElements", totalElements);
        pagination.put("totalPages", totalPages);
        
        response.put("pagination", pagination);
        return response;
    }
    
    /**
     * Creates a standardized partial success response for batch operations.
     * 
     * @param successData Data for successful operations
     * @param errorData Data for failed operations
     * @return Map containing both success and error information
     */
    public static Map<String, Object> partialSuccess(Object successData, Object errorData) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "partialSuccess");
        response.put("data", successData);
        response.put("errors", errorData);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}

package com.smartmess.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.smartmess.backend.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
	        ResourceNotFoundException exception,
	        HttpServletRequest request) {

	    ApiResponse<Void> response = ApiResponse.failure(
	            exception.getMessage(),
	            request.getRequestURI(),
	            null
	    );

	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(response);
	}
	
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(
	        BusinessException exception,
	        HttpServletRequest request) {

	    ApiResponse<Void> response = ApiResponse.failure(
	            exception.getMessage(),
	            request.getRequestURI(),
	            null
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(
	        Exception exception,
	        HttpServletRequest request) {

	    ApiResponse<Void> response = ApiResponse.failure(
	            "An unexpected error occurred.",
	            request.getRequestURI(),
	            null
	    );

	    return ResponseEntity
	            .status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(response);
	}
}

package com.smartmess.backend.exception;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.smartmess.backend.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Arrays;

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
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
	        MethodArgumentNotValidException exception,
	        HttpServletRequest request) {

	    Map<String, String> errors = new LinkedHashMap<>();

	    exception.getBindingResult()
	            .getFieldErrors()
	            .forEach(error ->
	                    errors.put(error.getField(), error.getDefaultMessage()));

	    ApiResponse<Map<String, String>> response = ApiResponse.failure(
	            "Validation failed.",
	            request.getRequestURI(),
	            errors
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleException(
	        Exception exception,
	        HttpServletRequest request) {

	    exception.printStackTrace();

	    ApiResponse<String> response = ApiResponse.failure(
	            exception.getMessage(),
	            request.getRequestURI(),
	            null
	    );

	    return ResponseEntity
	            .status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(response);
	}
	
	// for enum validation LUNCH / DINNER
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
	        MethodArgumentTypeMismatchException exception,
	        HttpServletRequest request) {

	    String message;

	    if (exception.getRequiredType() != null
	            && exception.getRequiredType().isEnum()) {

	        String allowedValues = Arrays.stream(exception.getRequiredType().getEnumConstants())
	                .map(Object::toString)
	                .reduce((first, second) -> first + ", " + second)
	                .orElse("");

	        message = String.format(
	                "Invalid value '%s' for '%s'. Allowed values are: %s.",
	                exception.getValue(),
	                exception.getName(),
	                allowedValues
	        );

	    } else {

	        message = String.format(
	                "Invalid value '%s' for parameter '%s'.",
	                exception.getValue(),
	                exception.getName()
	        );
	    }

	    ApiResponse<Void> response = ApiResponse.failure(
	            message,
	            request.getRequestURI(),
	            null
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
}

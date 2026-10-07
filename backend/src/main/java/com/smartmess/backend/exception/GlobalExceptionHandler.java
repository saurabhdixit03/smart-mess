package com.smartmess.backend.exception;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.smartmess.backend.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return failure(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {

        return failure(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiResponse<Map<String, String>> response =
                ApiResponse.failure(
                        "Validation failed.",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        return failure(
                HttpStatus.BAD_REQUEST,
                "Required parameter '"
                        + exception.getParameterName()
                        + "' is missing.",
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return failure(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request or invalid field value.",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String message;

        if (exception.getRequiredType() != null
                && exception.getRequiredType().isEnum()) {

            String allowedValues =
                    Arrays.stream(
                                    exception.getRequiredType().getEnumConstants()
                            )
                            .map(Object::toString)
                            .reduce(
                                    (first, second) -> first + ", " + second
                            )
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

        return failure(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request) {

        return failure(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource.",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        log.warn(
                "Database constraint conflict for {}",
                request.getRequestURI(),
                exception
        );

        String message =
                "This operation conflicts with existing data. "
                        + "Refresh and check the current state before trying again.";

        if (containsConstraint(
                exception,
                "uk_meal_records_mess_customer_menu"
        )) {

            message =
                    "This customer's meal has already been recorded "
                            + "for the selected menu.";
        }

        return failure(
                HttpStatus.CONFLICT,
                message,
                request
        );
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockingFailure(
            PessimisticLockingFailureException exception,
            HttpServletRequest request) {

        log.warn(
                "Database locking conflict for {}",
                request.getRequestURI(),
                exception
        );

        return failure(
                HttpStatus.CONFLICT,
                "Another operation is updating this data. "
                        + "Refresh and try again.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception exception,
            HttpServletRequest request) {

        log.error(
                "Unexpected error for {}",
                request.getRequestURI(),
                exception
        );

        return failure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    private ResponseEntity<ApiResponse<Void>> failure(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiResponse<Void> response =
                ApiResponse.failure(
                        message,
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /*
     * Database details are inspected internally, never returned
     * directly to the client.
     */
    private boolean containsConstraint(
            Throwable exception,
            String constraintName) {

        Throwable current = exception;

        while (current != null) {

            String message = current.getMessage();

            if (message != null
                    && message.contains(constraintName)) {
                return true;
            }

            Throwable cause = current.getCause();

            if (cause == current) {
                break;
            }

            current = cause;
        }

        return false;
    }
}
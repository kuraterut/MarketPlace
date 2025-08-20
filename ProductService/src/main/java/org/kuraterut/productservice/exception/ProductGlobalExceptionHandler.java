package org.kuraterut.productservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.productservice.dto.responses.ErrorResponse;
import org.kuraterut.productservice.exception.model.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ProductGlobalExceptionHandler {
    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDeniedException(PermissionDeniedException e) {
        log.warn("[ProductGlobalExceptionHandler]: handle PermissionDeniedException, message: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExists(CategoryAlreadyExistsException e) {
        log.warn("[ProductGlobalExceptionHandler]: handle CategoryAlreadyExistsException, message: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(HttpStatus.CONFLICT);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException e) {
        log.warn("[ProductGlobalExceptionHandler]: handle CategoryNotFoundException, message: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e) {
        log.warn("[ProductGlobalExceptionHandler]: handle ProductNotFoundException, message: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadyExists(ProductAlreadyExistsException e) {
        log.warn("[ProductGlobalExceptionHandler]: handle ProductAlreadyExistsException, message: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(HttpStatus.CONFLICT);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("[ProductGlobalExceptionHandler]: handle MethodArgumentNotValidException, message: {}", errors);

        ErrorResponse errorResponse = new ErrorResponse(
                errors, HttpStatus.BAD_REQUEST, OffsetDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        log.warn("[ProductGlobalExceptionHandler]: handle ConstraintViolationException, message: {}", errors);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errors, HttpStatus.BAD_REQUEST, OffsetDateTime.now()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("[ProductGlobalExceptionHandler]: handle BindException, message: {}", errors);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errors, HttpStatus.BAD_REQUEST, OffsetDateTime.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[ProductGlobalExceptionHandler]: handle AccessDeniedException, message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        List.of("Access denied"),
                        HttpStatus.FORBIDDEN,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("[ProductGlobalExceptionHandler]: handle AuthenticationException, message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        List.of("Authentication failed"),
                        HttpStatus.UNAUTHORIZED,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("[ProductGlobalExceptionHandler]: handle DataIntegrityViolationException, message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        List.of("Data conflict (duplicate or invalid data)"),
                        HttpStatus.CONFLICT,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException ex) {
        Throwable rootCause = ex.getRootCause();
        String errorMsg = (rootCause != null) ? rootCause.getMessage() : ex.getMessage();

        log.warn("[ProductGlobalExceptionHandler]: handle JpaSystemException, message: {}", errorMsg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        List.of("Database error: " + errorMsg),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[ProductGlobalExceptionHandler]: handle HttpMessageNotReadableException, message: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        List.of("Invalid request body"),
                        HttpStatus.BAD_REQUEST,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(NullPointerException ex) {
        log.warn("[ProductGlobalExceptionHandler]: handle NullPointerException, message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        List.of("Internal server error (NPE)"),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        OffsetDateTime.now())
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.warn("[ProductGlobalExceptionHandler]: handle Exception, message: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        error.setTimestamp(OffsetDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

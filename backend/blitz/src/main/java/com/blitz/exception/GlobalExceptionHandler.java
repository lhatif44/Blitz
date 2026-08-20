package com.blitz.exception;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.blitz.dto.ErrorResponse;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler{

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> missingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(InvalidComparisonException.class)
    ResponseEntity<ErrorResponse> badRequest(InvalidComparisonException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> internalError(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "An unexpected error   occurred.", Instant.now()));
    }
}
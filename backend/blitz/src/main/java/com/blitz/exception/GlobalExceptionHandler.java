package com.blitz.exception;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.blitz.dto.ErrorResponse;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler{
    
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) { 
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(InvalidComparisonException.class)
    ResponseEntity<ErrorResponse> badRequest(InvalidComparisonException e) { 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage(), Instant.now()));
    }
} 
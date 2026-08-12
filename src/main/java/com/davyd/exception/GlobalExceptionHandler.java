package com.davyd.exception;

import com.davyd.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundHandler(NotFoundException notFoundException){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = createErrorResponse(status, notFoundException);

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> insufficientFundsHandler(InsufficientFundsException exception){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(createErrorResponse(status, exception));
    }

    private ErrorResponse createErrorResponse(HttpStatus status, RuntimeException exception){
        return new ErrorResponse(status.value(), status.getReasonPhrase(), exception.getMessage(), LocalDateTime.now());
    }
}

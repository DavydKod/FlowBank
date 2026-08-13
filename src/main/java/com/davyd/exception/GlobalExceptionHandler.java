package com.davyd.exception;

import com.davyd.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleJakartaMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ){
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException exception){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.badRequest().body(createErrorResponse(status, exception));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleIllegalExceptions(RuntimeException exception){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.badRequest().body(createErrorResponse(status, exception));
    }

    private ErrorResponse createErrorResponse(HttpStatus status, RuntimeException exception){
        return new ErrorResponse(status.value(), status.getReasonPhrase(), exception.getMessage(), LocalDateTime.now());
    }
}

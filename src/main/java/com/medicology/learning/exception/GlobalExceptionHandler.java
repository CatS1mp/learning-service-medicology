package com.medicology.learning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Xử lý lỗi Sai Email/Mật khẩu (Cái lỗi 500 nãy của bạn)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
    log.warn(
        "IllegalArgumentException at [{} {}] - message: {}",
        request.getMethod(),
        request.getRequestURI(),
        ex.getMessage(),
        ex
    );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), 
                ex.getMessage(), 
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 2. Xử lý lỗi Validation (Cái lỗi "Email không hợp lệ" lúc đầu)
    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + "=" + err.getRejectedValue() + " (" + err.getDefaultMessage() + ")")
            .collect(Collectors.joining("; "));

        log.warn(
            "Validation failed at [{} {}] - message: {} - details: {}",
            request.getMethod(),
            request.getRequestURI(),
            errorMessage,
            fieldErrors,
            ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Xử lý tất cả các lỗi lạ khác (Tránh hiện Trace dài dòng)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error(
                "Unhandled exception at [{} {}] - type: {} - message: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getName(),
                ex.getMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Hệ thống gặp sự cố bất ngờ. Vui lòng thử lại sau!",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

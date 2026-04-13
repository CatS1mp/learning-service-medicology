package com.medicology.learning.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Invalid request");
        String details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));

        log.warn(
                "Constraint violation at [{} {}] - message: {} - details: {}",
                request.getMethod(),
                request.getRequestURI(),
                errorMessage,
                details,
                ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(InvalidFileException ex, HttpServletRequest request) {
        log.warn(
                "Invalid file at [{} {}] - message: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex, HttpServletRequest request) {
        log.warn(
                "Invalid request at [{} {}] - message: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        Throwable rootCause = getRootCause(ex);
        String duplicateMessage = extractDuplicateMessage(rootCause);
        HttpStatus status = duplicateMessage != null ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        String responseMessage = duplicateMessage != null
                ? duplicateMessage
                : "Du lieu khong hop le hoac vi pham rang buoc trong database.";

        log.warn(
                "Data integrity violation at [{} {}] - message: {} - root: {}",
                request.getMethod(),
                request.getRequestURI(),
                responseMessage,
                rootCause != null ? rootCause.getMessage() : ex.getMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                status.value(),
                responseMessage,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(StorageUploadException.class)
    public ResponseEntity<ErrorResponse> handleStorageUploadException(StorageUploadException ex, HttpServletRequest request) {
        log.error(
                "Storage upload failed at [{} {}] - message: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                "Khong the tai anh len storage. Vui long thu lai sau.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
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

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String extractDuplicateMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        String message = throwable.getMessage();
        boolean isDuplicateKey = throwable instanceof SQLException sqlException
                ? "23505".equals(sqlException.getSQLState())
                : message != null && message.contains("duplicate key value violates unique constraint");

        if (!isDuplicateKey || message == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]*)\\) already exists\\.").matcher(message);
        if (!matcher.find()) {
            return "Du lieu bi trung voi mot ban ghi da ton tai.";
        }

        String field = matcher.group(1);
        String value = matcher.group(2);

        if ("slug".equalsIgnoreCase(field)) {
            return "Slug '" + value + "' da ton tai.";
        }

        return "Gia tri '" + value + "' cua truong '" + humanizeField(field) + "' da ton tai.";
    }

    private String humanizeField(String field) {
        return field.replace('_', ' ').toLowerCase(Locale.ROOT);
    }
}

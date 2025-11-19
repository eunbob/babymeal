package com.example.devkorproject.common.exception;

import com.example.devkorproject.common.constants.ErrorCode;
import com.example.devkorproject.common.dto.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<Object> general(GeneralException e, WebRequest request) {
        log.warn("GeneralException: {}", e.getMessage(), e);
        return handleExceptionInternal(e, e.getErrorCode(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        log.error("Unhandled exception", e);
        return handleExceptionInternal(e, ErrorCode.INTERNAL_ERROR, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, ErrorCode.valueOf(status), headers, status, request);
    }


    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode, WebRequest request) {
        return handleExceptionInternal(e, errorCode, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(
                e,
                HttpResponse.of(false, errorCode.getCode(), errorCode.getMessage(e.getMessage())),
                headers,
                status,
                request
        );
    }
}

package com.api.auth.Application.Exceptions;

import com.api.auth.Application.DTOs.Common.ErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        Object details = ex instanceof ValidationException
                ? ((ValidationException) ex).getErrors()
                : null;

        var body = new ErrorDTO(
                ex.getStatus().value(),
                ex.getMessage(),
                details
        );

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {

        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .toList();

        var body = new ErrorDTO(
                400,
                "Erro de validação",
                errors
        );

        return ResponseEntity.badRequest().body(body);
    }
}

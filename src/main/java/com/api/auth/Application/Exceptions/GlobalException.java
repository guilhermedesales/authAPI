package com.api.auth.Application.Exceptions;

import com.api.auth.Application.DTOs.Common.ErrorDTO;
import com.api.auth.Application.Utils.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        Object details = ex instanceof ValidationException
                ? ((ValidationException) ex).getErrors()
                : null;

        if (ex.getStatus().is5xxServerError()) {
            log.error("[ERRO] Excecao de aplicacao tratada - status={} mensagem={}", ex.getStatus().value(), ex.getMessage(), ex);
        } else {
            log.warn("[ERRO] Excecao de aplicacao tratada - status={} mensagem={}", ex.getStatus().value(), ex.getMessage());
        }

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
                "Erro de validacao",
                errors
        );

        log.warn("[ERRO] Validacao de bean falhou - totalErros={}", errors.size());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleUnexpectedException(Exception ex) {
        log.error("[ERRO] Excecao inesperada - tipo={} mensagem={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        var body = new ErrorDTO(
                500,
                ErrorMessages.Sistema.ERRO_INTERNO,
                null
        );

        return ResponseEntity.internalServerError().body(body);
    }
}

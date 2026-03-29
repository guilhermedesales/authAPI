package com.api.auth.Application.Exceptions;
import org.springframework.http.HttpStatus;
import java.util.List;

public class ValidationException extends AppException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Erro de validação", HttpStatus.BAD_REQUEST);
        this.errors = errors;
    }

    public ValidationException(String error) {
        super("Erro de validação", HttpStatus.BAD_REQUEST);
        this.errors = List.of(error);
    }

    public List<String> getErrors() {
        return errors;
    }
}

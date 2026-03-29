package com.api.auth.Application.DTOs.Common;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorDTO {

    private int status;
    private String message;
    private Object details;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorDTO(int status, String message, Object details) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}

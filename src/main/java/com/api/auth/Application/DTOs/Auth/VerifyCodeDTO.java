package com.api.auth.Application.DTOs.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class VerifyCodeDTO {
    @NotBlank
    private String code;
}

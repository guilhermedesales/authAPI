package com.api.auth.Application.DTOs.Auth.RefreshToken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    @Schema(example = "7fd7a42a-a741-4a26-b983-2e8b39c88943:5fa5ca29-a4ff-45f3-861f-7ff2cb7fd0cc")
    @NotBlank(message = "O refresh token é obrigatório")
    private String refreshToken;
}

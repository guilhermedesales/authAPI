package com.api.auth.Application.DTOs.Auth.RefreshToken;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(example = "7fd7a42a-a741-4a26-b983-2e8b39c88943:5fa5ca29-a4ff-45f3-861f-7ff2cb7fd0cc")
    private String refreshToken;
}

package com.api.auth.Application.DTOs.UserDevice;

import com.api.auth.Domain.Enum.SessionTrustLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserDeviceDTO {

    private UUID deviceId;
    private String deviceName;
    private String location;
    private SessionTrustLevel trustLevel;
    private Integer riskScore;
    private Instant lastUsedAt;

}

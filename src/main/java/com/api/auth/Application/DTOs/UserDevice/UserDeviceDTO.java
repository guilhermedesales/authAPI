package com.api.auth.Application.DTOs.UserDevice;

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
    private Instant lastUsedAt;

}

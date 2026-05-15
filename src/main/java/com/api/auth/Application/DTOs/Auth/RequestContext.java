package com.api.auth.Application.DTOs.Auth;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class RequestContext {
    private String ip;
    private String userAgent;
    private UUID deviceId;
}

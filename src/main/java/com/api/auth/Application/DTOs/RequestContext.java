package com.api.auth.Application.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RequestContext {
    private String ip;
    private String userAgent;
}

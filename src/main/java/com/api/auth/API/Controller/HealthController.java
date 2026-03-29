package com.api.auth.API.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/validateJWT")
    public Map<String, String> validateToken() {
        return Map.of("message", "JWT funcionando ✅");
    }
}
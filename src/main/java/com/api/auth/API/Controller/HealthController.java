package com.api.auth.API.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/validateJWT")
    public Map<String, String> validateToken() {
        log.debug("[HEALTH] JWT validation endpoint accessed");
        return Map.of("message", "JWT funcionando ✅");
    }
}
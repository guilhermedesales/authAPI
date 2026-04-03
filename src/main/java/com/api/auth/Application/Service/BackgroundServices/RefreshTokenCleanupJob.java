package com.api.auth.Application.Service.BackgroundServices;

import com.api.auth.Application.Service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final JwtService jwtService;

    @Scheduled(cron = "${auth.refresh-token.cleanup-cron:0 0 3 * * *}")
    public void cleanupExpiredRefreshTokens() {
        int deleted = jwtService.deleteExpiredTokens();
        log.debug("[AUTH] Scheduled refresh token cleanup executed - deleted={}", deleted);
    }
}

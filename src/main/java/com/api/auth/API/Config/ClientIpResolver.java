package com.api.auth.API.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    private static final String[] FORWARDED_IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP"
    };

    @Value("${auth.rate-limit.trust-forwarded-headers:true}")
    private boolean trustForwardedHeaders;

    public String resolve(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            for (String header : FORWARDED_IP_HEADERS) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isBlank()) {
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    return normalizeIp(ip);
                }
            }
        }

        return normalizeIp(request.getRemoteAddr());
    }

    private String normalizeIp(String ip) {
        if (ip == null) {
            return null;
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }

        return ip;
    }
}

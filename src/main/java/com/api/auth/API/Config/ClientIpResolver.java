package com.api.auth.API.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

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

    @Value("${auth.rate-limit.trusted-proxies:127.0.0.1,::1}")
    private String trustedProxies;

    public String resolve(HttpServletRequest request) {
        String remoteAddr = normalizeIp(request.getRemoteAddr());

        if (trustForwardedHeaders && isTrustedProxy(remoteAddr)) {
            for (String header : FORWARDED_IP_HEADERS) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isBlank()) {
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    String normalized = normalizeIp(ip);
                    if (normalized != null) {
                        return normalized;
                    }
                }
            }
        }

        return remoteAddr;
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

        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) {
            return false;
        }

        List<String> trustedEntries = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        for (String entry : trustedEntries) {
            if ("*".equals(entry)) {
                return true;
            }

            if (entry.contains("/")) {
                if (isInCidrRange(remoteAddr, entry)) {
                    return true;
                }
                continue;
            }

            String normalizedTrustedIp = normalizeIp(entry);
            if (normalizedTrustedIp != null && normalizedTrustedIp.equals(remoteAddr)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInCidrRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress inetAddress = InetAddress.getByName(ip);
            InetAddress networkAddress = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);

            byte[] ipBytes = inetAddress.getAddress();
            byte[] networkBytes = networkAddress.getAddress();
            if (ipBytes.length != networkBytes.length) {
                return false;
            }

            BigInteger ipValue = new BigInteger(1, ipBytes);
            BigInteger networkValue = new BigInteger(1, networkBytes);
            int bits = ipBytes.length * 8;

            BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE)
                    .shiftRight(bits - prefix)
                    .shiftLeft(bits - prefix);

            return ipValue.and(mask).equals(networkValue.and(mask));
        } catch (Exception ignored) {
            return false;
        }
    }
}

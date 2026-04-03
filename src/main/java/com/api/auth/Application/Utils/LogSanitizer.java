package com.api.auth.Application.Utils;

import java.util.UUID;

public final class LogSanitizer {

    private LogSanitizer() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int at = email.indexOf('@');
        if (at <= 1 || at == email.length() - 1) {
            return "***";
        }

        String first = email.substring(0, 1);
        String domain = email.substring(at);
        return first + "***" + domain;
    }

    public static String shortId(UUID id) {
        if (id == null) {
            return "null";
        }
        String raw = id.toString();
        return raw.substring(0, 8);
    }
}


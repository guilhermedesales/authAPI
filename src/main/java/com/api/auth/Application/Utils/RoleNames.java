package com.api.auth.Application.Utils;

import java.util.Locale;

public final class RoleNames {

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String GLOBAL_ADMIN = "GLOBAL_ADMIN";

    private RoleNames() {
    }

    public static String toAuthority(String roleName) {
        return "ROLE_" + roleName.toUpperCase(Locale.ROOT);
    }
}


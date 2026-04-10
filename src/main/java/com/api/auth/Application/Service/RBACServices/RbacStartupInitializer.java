package com.api.auth.Application.Service.RBACServices;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RbacStartupInitializer {

    private final RbacBootstrapService rbacBootstrapService;

    public RbacStartupInitializer(RbacBootstrapService rbacBootstrapService) {
        this.rbacBootstrapService = rbacBootstrapService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRbacDefaults() {
        rbacBootstrapService.ensureDefaultsForAllSystems();
    }
}


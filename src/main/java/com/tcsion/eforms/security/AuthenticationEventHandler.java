package com.tcsion.eforms.security;

import com.tcsion.eforms.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEventHandler {

    private final AuditService auditService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        auditService.logAs(username, "LOGIN_SUCCESS", "USER", null, null, null, null, "WEB");
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        auditService.logAs(username, "LOGIN_FAILURE", "USER", null, null,
                null, event.getException().getMessage(), "WEB");
    }
}

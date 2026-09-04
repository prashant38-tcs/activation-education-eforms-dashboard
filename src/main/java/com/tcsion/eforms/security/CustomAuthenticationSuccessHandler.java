package com.tcsion.eforms.security;

import com.tcsion.eforms.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        loginAttemptService.onSuccessfulLogin(username, request.getRemoteAddr());
        auditService.logAs(username, "LOGIN_SUCCESS", "USER", null, null, null, null, "WEB");

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        if (principal.isForcePasswordChange()) {
            response.sendRedirect(request.getContextPath() + "/change-password?firstLogin=true");
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

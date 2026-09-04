package com.tcsion.eforms.security;

import com.tcsion.eforms.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String errorCode;
        if (exception instanceof LockedException) {
            errorCode = "locked";
        } else if (exception instanceof DisabledException) {
            errorCode = "disabled";
        } else {
            if (username != null && !username.trim().isEmpty()) {
                loginAttemptService.onFailedLogin(username.trim());
            }
            errorCode = "bad_credentials";
        }
        if (username != null && !username.trim().isEmpty()) {
            auditService.logAs(username.trim(), "LOGIN_FAILURE", "USER", null, null, null, exception.getMessage(), "WEB");
        }
        setDefaultFailureUrl("/login?error=" + errorCode);
        super.onAuthenticationFailure(request, response, exception);
    }
}

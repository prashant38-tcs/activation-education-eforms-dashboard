package com.tcsion.eforms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<CustomUserDetails> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return Optional.empty();
        }
        return Optional.of((CustomUserDetails) auth.getPrincipal());
    }

    public static Long currentUserId() {
        return currentUser().map(CustomUserDetails::getUserId).orElse(null);
    }

    public static String currentUsername() {
        return currentUser().map(CustomUserDetails::getUsername).orElse("SYSTEM");
    }

    public static boolean currentUserHasRole(String roleCode) {
        return currentUser().map(u -> u.hasRole(roleCode)).orElse(false);
    }

    public static Optional<HttpServletRequest> currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? Optional.empty() : Optional.of(attrs.getRequest());
    }

    public static String currentIpAddress() {
        return currentRequest().map(HttpServletRequest::getRemoteAddr).orElse(null);
    }

    public static String currentUserAgent() {
        return currentRequest().map(r -> r.getHeader("User-Agent")).orElse(null);
    }
}

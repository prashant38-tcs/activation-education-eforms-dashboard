package com.tcsion.eforms.security;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.exception.TicketAccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Object-level authorization for tickets: a developer must never be able to
 * retrieve another developer's ticket by changing the URL or request
 * parameters, even though the URL pattern itself may be accessible to the
 * DEVELOPER role. This check is applied inside every service method that
 * loads a single ticket for a developer-facing operation.
 */
@Component
public class TicketAccessGuard {

    public void assertCanView(Ticket ticket) {
        CustomUserDetails user = SecurityUtils.currentUser()
                .orElseThrow(() -> new TicketAccessDeniedException("Authentication is required to view this ticket."));

        if (hasAdministrativeAccess(user)) {
            return;
        }
        if (user.hasRole(Role.DEVELOPER)) {
            boolean isOwner = ticket.getAssignedUser() != null && ticket.getAssignedUser().getId().equals(user.getUserId());
            if (!isOwner) {
                throw new TicketAccessDeniedException(
                        "You are not authorized to view ticket " + ticket.getTicketNumber() + ".");
            }
            return;
        }
        throw new TicketAccessDeniedException("You are not authorized to view this ticket.");
    }

    public void assertCanModifyAsDeveloper(Ticket ticket) {
        CustomUserDetails user = SecurityUtils.currentUser()
                .orElseThrow(() -> new TicketAccessDeniedException("Authentication is required."));
        if (hasAdministrativeAccess(user)) {
            return;
        }
        boolean isOwner = ticket.getAssignedUser() != null && ticket.getAssignedUser().getId().equals(user.getUserId());
        if (!isOwner) {
            throw new TicketAccessDeniedException(
                    "You are not authorized to update ticket " + ticket.getTicketNumber() + ".");
        }
    }

    public boolean hasAdministrativeAccess(CustomUserDetails user) {
        return user.hasRole(Role.TEAM_LEAD) || user.hasRole(Role.TECHNICAL_LEAD)
                || user.hasRole(Role.DASHBOARD_HANDLER) || user.hasRole(Role.SYSTEM_ADMIN);
    }
}

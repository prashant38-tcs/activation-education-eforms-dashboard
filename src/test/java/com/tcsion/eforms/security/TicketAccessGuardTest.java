package com.tcsion.eforms.security;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.exception.TicketAccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the critical security invariant: a developer cannot retrieve
 * another developer's ticket, even though the DEVELOPER role is generally
 * permitted to view ticket detail pages.
 */
class TicketAccessGuardTest {

    private final TicketAccessGuard guard = new TicketAccessGuard();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String username, String... roleCodes) {
        User user = User.builder().id(userId).username(username).fullName(username).passwordHash("x").active(true).build();
        Set<Role> roles = new HashSet<>();
        for (String code : roleCodes) {
            roles.add(Role.builder().roleCode(code).roleName(code).build());
        }
        user.setRoles(roles);
        CustomUserDetails principal = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Ticket ticketAssignedTo(Long assignedUserId) {
        User assignee = User.builder().id(assignedUserId).username("dev" + assignedUserId).fullName("Dev").passwordHash("x").build();
        return Ticket.builder().id(100L).ticketNumber("EF-1001").assignedUser(assignee).build();
    }

    @Test
    void developerCannotViewAnotherDevelopersTicket() {
        authenticateAs(2L, "developer.two", Role.DEVELOPER);
        Ticket ticketOwnedByDeveloperOne = ticketAssignedTo(1L);

        assertThrows(TicketAccessDeniedException.class, () -> guard.assertCanView(ticketOwnedByDeveloperOne));
    }

    @Test
    void developerCanViewOwnTicket() {
        authenticateAs(1L, "developer.one", Role.DEVELOPER);
        Ticket ownTicket = ticketAssignedTo(1L);

        assertDoesNotThrow(() -> guard.assertCanView(ownTicket));
    }

    @Test
    void developerCannotModifyAnotherDevelopersTicket() {
        authenticateAs(2L, "developer.two", Role.DEVELOPER);
        Ticket ticketOwnedByDeveloperOne = ticketAssignedTo(1L);

        assertThrows(TicketAccessDeniedException.class, () -> guard.assertCanModifyAsDeveloper(ticketOwnedByDeveloperOne));
    }

    @Test
    void teamLeadCanViewAnyTicket() {
        authenticateAs(99L, "team.lead", Role.TEAM_LEAD);
        Ticket someonesTicket = ticketAssignedTo(1L);

        assertDoesNotThrow(() -> guard.assertCanView(someonesTicket));
    }

    @Test
    void technicalLeadCanViewAnyTicket() {
        authenticateAs(98L, "tech.lead", Role.TECHNICAL_LEAD);
        Ticket someonesTicket = ticketAssignedTo(1L);

        assertDoesNotThrow(() -> guard.assertCanView(someonesTicket));
    }

    @Test
    void unauthenticatedAccessIsDenied() {
        Ticket someonesTicket = ticketAssignedTo(1L);
        assertThrows(TicketAccessDeniedException.class, () -> guard.assertCanView(someonesTicket));
    }
}

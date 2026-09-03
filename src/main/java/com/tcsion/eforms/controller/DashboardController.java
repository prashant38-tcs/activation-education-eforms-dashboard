package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.AgingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TicketRepository ticketRepository;
    private final AgingService agingService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        boolean isAdmin = SecurityUtils.currentUserHasRole(Role.TEAM_LEAD)
                || SecurityUtils.currentUserHasRole(Role.TECHNICAL_LEAD)
                || SecurityUtils.currentUserHasRole(Role.DASHBOARD_HANDLER)
                || SecurityUtils.currentUserHasRole(Role.SYSTEM_ADMIN);
        return isAdmin ? adminDashboard(model) : developerDashboard(model);
    }

    private String developerDashboard(Model model) {
        Long userId = SecurityUtils.currentUserId();
        List<Ticket> myTickets = ticketRepository.findOpenActiveByDeveloper(userId);

        model.addAttribute("totalTickets", myTickets.size());
        model.addAttribute("wipCount", countByStatus(myTickets, "WORK_IN_PROGRESS"));
        model.addAttribute("uatCount", countByStatus(myTickets, "UAT_IN_PROGRESS"));
        model.addAttribute("qaCount", countByStatus(myTickets, "QA_IN_PROGRESS"));
        model.addAttribute("onHoldCount", myTickets.stream().filter(Ticket::isOnHold).count());
        model.addAttribute("frameworkDependencyCount", countByStatus(myTickets, "REASSIGNED_TO_FRAMEWORK_TEAM"));
        model.addAttribute("otherTeamDependencyCount", countByStatus(myTickets, "REASSIGNED_TO_OTHER_TEAM"));
        model.addAttribute("overdueCount", myTickets.stream()
                .filter(t -> t.getEstimatedProductionDate() != null && t.getEstimatedProductionDate().isBefore(LocalDate.now()))
                .count());
        model.addAttribute("highRiskCount", myTickets.stream().filter(t -> "HIGH".equals(t.getSlaRiskCategory())).count());
        model.addAttribute("noUpdateCount", myTickets.stream()
                .filter(t -> t.getLastActivityDate() == null || t.getLastActivityDate().isBefore(LocalDateTime.now().minusDays(3)))
                .count());
        model.addAttribute("recentTickets", myTickets.stream().limit(10).collect(Collectors.toList()));
        return "dashboard/developer-dashboard";
    }

    private String adminDashboard(Model model) {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        model.addAttribute("totalOpenTickets", ticketRepository.countAllOpen());
        model.addAttribute("newTicketsToday", ticketRepository.countCreatedBetween(todayStart, todayEnd));
        model.addAttribute("movedToProductionToday", ticketRepository.countMovedToProductionOn(LocalDate.now()));
        model.addAttribute("onHoldCount", ticketRepository.countOnHold());
        model.addAttribute("wipCount", countByStatus(openTickets, "WORK_IN_PROGRESS"));
        model.addAttribute("uatCount", countByStatus(openTickets, "UAT_IN_PROGRESS"));
        model.addAttribute("qaCount", countByStatus(openTickets, "QA_IN_PROGRESS"));
        model.addAttribute("frameworkPendingCount", countByStatus(openTickets, "REASSIGNED_TO_FRAMEWORK_TEAM"));
        model.addAttribute("otherTeamPendingCount", countByStatus(openTickets, "REASSIGNED_TO_OTHER_TEAM"));
        model.addAttribute("overdueCount", openTickets.stream()
                .filter(t -> t.getEstimatedProductionDate() != null && t.getEstimatedProductionDate().isBefore(LocalDate.now()))
                .count());
        model.addAttribute("slaAtRiskCount", openTickets.stream().filter(t -> "AT_RISK".equals(t.getSlaState())).count());
        model.addAttribute("slaBreachedCount", openTickets.stream().filter(t -> "BREACHED".equals(t.getSlaState())).count());
        model.addAttribute("noUpdateCount", openTickets.stream()
                .filter(t -> t.getLastActivityDate() == null || t.getLastActivityDate().isBefore(LocalDateTime.now().minusDays(3)))
                .count());
        model.addAttribute("agingBuckets", agingService.getAgingBucketCounts());
        model.addAttribute("recentTickets", openTickets.stream().limit(10).collect(Collectors.toList()));
        return "dashboard/admin-dashboard";
    }

    private long countByStatus(List<Ticket> tickets, String statusCode) {
        return tickets.stream().filter(t -> t.getCurrentStatus() != null
                && statusCode.equals(t.getCurrentStatus().getStatusCode())).count();
    }
}

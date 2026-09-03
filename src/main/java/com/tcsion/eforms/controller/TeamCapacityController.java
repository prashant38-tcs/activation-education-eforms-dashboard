package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/team-capacity")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
public class TeamCapacityController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    private static final int NORMAL_THRESHOLD = 5;
    private static final int HIGH_THRESHOLD = 8;
    private static final int CRITICAL_THRESHOLD = 12;

    @GetMapping
    public String teamCapacity(Model model) {
        List<User> developers = userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (User dev : developers) {
            List<Ticket> myTickets = ticketRepository.findOpenActiveByDeveloper(dev.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("developer", dev);
            row.put("totalActive", myTickets.size());
            row.put("wip", countStatus(myTickets, "WORK_IN_PROGRESS"));
            row.put("uat", countStatus(myTickets, "UAT_IN_PROGRESS"));
            row.put("qa", countStatus(myTickets, "QA_IN_PROGRESS"));
            row.put("onHold", myTickets.stream().filter(Ticket::isOnHold).count());
            row.put("frameworkPending", countStatus(myTickets, "REASSIGNED_TO_FRAMEWORK_TEAM"));
            row.put("otherTeamPending", countStatus(myTickets, "REASSIGNED_TO_OTHER_TEAM"));
            row.put("highRisk", myTickets.stream().filter(t -> "HIGH".equals(t.getSlaRiskCategory())).count());
            row.put("overdue", myTickets.stream().filter(t -> t.getEstimatedProductionDate() != null
                    && t.getEstimatedProductionDate().isBefore(LocalDate.now())).count());
            row.put("noUpdate", myTickets.stream().filter(t -> t.getLastActivityDate() == null
                    || t.getLastActivityDate().isBefore(LocalDateTime.now().minusDays(3))).count());
            row.put("workloadIndicator", workloadIndicator(myTickets.size()));
            rows.add(row);
        }
        model.addAttribute("rows", rows);
        return "dashboard/team-capacity";
    }

    private long countStatus(List<Ticket> tickets, String statusCode) {
        return tickets.stream().filter(t -> t.getCurrentStatus() != null
                && statusCode.equals(t.getCurrentStatus().getStatusCode())).count();
    }

    private String workloadIndicator(int totalActive) {
        if (totalActive >= CRITICAL_THRESHOLD) return "Critical";
        if (totalActive >= HIGH_THRESHOLD) return "High";
        if (totalActive >= NORMAL_THRESHOLD) return "Normal";
        return "Available";
    }
}

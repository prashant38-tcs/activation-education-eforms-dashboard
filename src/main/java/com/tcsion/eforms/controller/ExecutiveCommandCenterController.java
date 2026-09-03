package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.ImportBatchRepository;
import com.tcsion.eforms.repository.TicketAssignmentRepository;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.AgingService;
import com.tcsion.eforms.service.WsrService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/executive-command-center")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
public class ExecutiveCommandCenterController {

    private final TicketRepository ticketRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final ImportBatchRepository importBatchRepository;
    private final AgingService agingService;
    private final WsrService wsrService;

    @GetMapping
    public String commandCenter(Model model) {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();

        model.addAttribute("totalOpen", openTickets.size());
        model.addAttribute("criticalAging", openTickets.stream().filter(t -> t.getAgingDays() >= 16).count());
        model.addAttribute("highSlaRisk", openTickets.stream().filter(t -> "HIGH".equals(t.getSlaRiskCategory())).count());
        model.addAttribute("slaBreached", openTickets.stream().filter(t -> "BREACHED".equals(t.getSlaState())).count());
        model.addAttribute("productionMovements", ticketRepository.countMovedToProductionOn(LocalDate.now()));
        model.addAttribute("noUpdate", openTickets.stream().filter(t -> t.getLastActivityDate() == null
                || t.getLastActivityDate().isBefore(LocalDateTime.now().minusDays(3))).count());

        Map<String, Long> customerDistribution = openTickets.stream()
                .filter(t -> t.getCustomer() != null)
                .collect(Collectors.groupingBy(t -> t.getCustomer().getCustomerName(), Collectors.counting()));
        Map<String, Long> statusDistribution = openTickets.stream()
                .filter(t -> t.getCurrentStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getCurrentStatus().getDisplayName(), Collectors.counting()));
        Map<String, Long> agingDistribution = agingService.getAgingBucketCounts();
        Map<String, Long> dependencyDistribution = openTickets.stream()
                .filter(t -> t.getDependencyTeam() != null)
                .collect(Collectors.groupingBy(t -> t.getDependencyTeam().getTeamName(), Collectors.counting()));

        model.addAttribute("customerDistribution", customerDistribution);
        model.addAttribute("statusDistribution", statusDistribution);
        model.addAttribute("agingDistribution", agingDistribution);
        model.addAttribute("dependencyDistribution", dependencyDistribution);

        model.addAttribute("teamWsrToday", wsrService.getTeamWsr(LocalDate.now(), LocalDate.now()));
        model.addAttribute("recentReassignments", ticketAssignmentRepository
                .findByAssignedAtAfterOrderByAssignedAtDesc(LocalDateTime.now().minusDays(7)));
        model.addAttribute("upcomingProductionDates", ticketRepository
                .findApproachingProductionDate(LocalDate.now(), LocalDate.now().plusDays(3)));
        model.addAttribute("missedProductionDates", ticketRepository.findOverdueTickets(LocalDate.now()));
        model.addAttribute("recentImports", importBatchRepository.findAllByOrderByUploadedAtDesc()
                .stream().limit(5).collect(Collectors.toList()));

        return "dashboard/executive-command-center";
    }
}

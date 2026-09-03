package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.SlaRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sla-risk")
@RequiredArgsConstructor
public class SlaRiskController {

    private final SlaRiskService slaRiskService;
    private final TicketRepository ticketRepository;

    @GetMapping
    public String slaDashboard(Model model) {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();
        long met = openTickets.stream().filter(t -> "MET".equals(t.getSlaState())).count();
        long atRisk = openTickets.stream().filter(t -> "AT_RISK".equals(t.getSlaState())).count();
        long breached = openTickets.stream().filter(t -> "BREACHED".equals(t.getSlaState())).count();

        Map<String, Long> riskByCustomer = openTickets.stream()
                .filter(t -> t.getCustomer() != null && "HIGH".equals(t.getSlaRiskCategory()))
                .collect(Collectors.groupingBy(t -> t.getCustomer().getCustomerName(), Collectors.counting()));
        Map<String, Long> riskByStatus = openTickets.stream()
                .filter(t -> t.getCurrentStatus() != null && "HIGH".equals(t.getSlaRiskCategory()))
                .collect(Collectors.groupingBy(t -> t.getCurrentStatus().getDisplayName(), Collectors.counting()));

        model.addAttribute("slaMet", met);
        model.addAttribute("slaAtRisk", atRisk);
        model.addAttribute("slaBreached", breached);
        model.addAttribute("complianceRate", openTickets.isEmpty() ? 100
                : Math.round((met * 100.0) / openTickets.size()));
        model.addAttribute("highRiskTickets", slaRiskService.getHighRiskTickets());
        model.addAttribute("riskByCustomer", riskByCustomer);
        model.addAttribute("riskByStatus", riskByStatus);
        return "dashboard/sla-risk-dashboard";
    }

    @GetMapping("/{ticketId}/explain")
    public String explain(@PathVariable Long ticketId, Model model) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        model.addAttribute("ticket", ticket);
        model.addAttribute("reasons", slaRiskService.explainRisk(ticket));
        return "dashboard/sla-risk-explain";
    }
}

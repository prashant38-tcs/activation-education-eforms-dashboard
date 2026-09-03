package com.tcsion.eforms.controller;

import com.tcsion.eforms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/master-data")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
public class MasterDataController {

    private final CustomerMasterRepository customerMasterRepository;
    private final TicketTypeMasterRepository ticketTypeMasterRepository;
    private final PriorityMasterRepository priorityMasterRepository;
    private final SeverityMasterRepository severityMasterRepository;
    private final StatusMasterRepository statusMasterRepository;
    private final ActivityTypeMasterRepository activityTypeMasterRepository;
    private final TeamMasterRepository teamMasterRepository;
    private final AgingThresholdConfigRepository agingThresholdConfigRepository;
    private final SlaThresholdConfigRepository slaThresholdConfigRepository;
    private final RiskWeightConfigRepository riskWeightConfigRepository;
    private final DeploymentEnvironmentMasterRepository deploymentEnvironmentMasterRepository;
    private final AttachmentCategoryMasterRepository attachmentCategoryMasterRepository;

    @GetMapping
    public String masterDataHome(Model model) {
        model.addAttribute("customers", customerMasterRepository.findAll());
        model.addAttribute("ticketTypes", ticketTypeMasterRepository.findAll());
        model.addAttribute("priorities", priorityMasterRepository.findAll());
        model.addAttribute("severities", severityMasterRepository.findAll());
        model.addAttribute("statuses", statusMasterRepository.findAll());
        model.addAttribute("activityTypes", activityTypeMasterRepository.findAll());
        model.addAttribute("teams", teamMasterRepository.findAll());
        model.addAttribute("agingThresholds", agingThresholdConfigRepository.findAll());
        model.addAttribute("slaThresholds", slaThresholdConfigRepository.findAll());
        model.addAttribute("riskWeights", riskWeightConfigRepository.findAll());
        model.addAttribute("environments", deploymentEnvironmentMasterRepository.findAll());
        model.addAttribute("attachmentCategories", attachmentCategoryMasterRepository.findAll());
        return "admin/master-data";
    }

    @PostMapping("/customers/{id}/toggle")
    public String toggleCustomer(@PathVariable Long id) {
        customerMasterRepository.findById(id).ifPresent(c -> {
            c.setActive(!c.isActive());
            customerMasterRepository.save(c);
        });
        return "redirect:/master-data";
    }

    @PostMapping("/teams/{id}/toggle")
    public String toggleTeam(@PathVariable Long id) {
        teamMasterRepository.findById(id).ifPresent(t -> {
            t.setActive(!t.isActive());
            teamMasterRepository.save(t);
        });
        return "redirect:/master-data";
    }
}

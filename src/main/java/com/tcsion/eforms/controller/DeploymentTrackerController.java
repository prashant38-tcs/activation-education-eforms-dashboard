package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.DeploymentDetail;
import com.tcsion.eforms.repository.DeploymentDetailRepository;
import com.tcsion.eforms.repository.DeploymentEnvironmentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/deployment-tracker")
@RequiredArgsConstructor
public class DeploymentTrackerController {

    private final DeploymentDetailRepository deploymentDetailRepository;
    private final DeploymentEnvironmentMasterRepository deploymentEnvironmentMasterRepository;

    @GetMapping
    public String deploymentTracker(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String environment, Model model) {
        List<DeploymentDetail> deployments;
        if (status != null) {
            deployments = deploymentDetailRepository.findByDeploymentStatusOrderByDeploymentDateDesc(status);
        } else if (environment != null) {
            deployments = deploymentDetailRepository.findByEnvironmentOrderByDeploymentDateDesc(environment);
        } else {
            deployments = deploymentDetailRepository.findAll();
        }
        model.addAttribute("deployments", deployments);
        model.addAttribute("environments", deploymentEnvironmentMasterRepository.findByActiveTrueOrderBySortOrderAsc());
        return "dashboard/deployment-tracker";
    }
}

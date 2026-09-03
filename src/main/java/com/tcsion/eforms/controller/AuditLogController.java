package com.tcsion.eforms.controller;

import com.tcsion.eforms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','SYSTEM_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String auditLogs(@RequestParam(required = false) String action,
                             @RequestParam(required = false) String ticketNumber,
                             @RequestParam(required = false) String userName,
                             @RequestParam(defaultValue = "0") int page, Model model) {
        Specification<com.tcsion.eforms.entity.AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null && !action.isEmpty()) predicates.add(cb.equal(root.get("action"), action));
            if (ticketNumber != null && !ticketNumber.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("ticketNumber")), "%" + ticketNumber.toLowerCase() + "%"));
            if (userName != null && !userName.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("userName")), "%" + userName.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<com.tcsion.eforms.entity.AuditLog> logs = auditLogRepository.findAll(spec,
                PageRequest.of(page, 50, Sort.by("createdAt").descending()));
        model.addAttribute("logs", logs);
        return "admin/audit-logs";
    }
}

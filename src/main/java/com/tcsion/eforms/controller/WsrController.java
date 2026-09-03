package com.tcsion.eforms.controller;

import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.WsrService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/wsr")
@RequiredArgsConstructor
public class WsrController {

    private final WsrService wsrService;

    @GetMapping("/daily")
    public String dailyWsr(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        Long userId = SecurityUtils.currentUserId();
        boolean isAdmin = SecurityUtils.currentUserHasRole("TEAM_LEAD") || SecurityUtils.currentUserHasRole("TECHNICAL_LEAD")
                || SecurityUtils.currentUserHasRole("DASHBOARD_HANDLER") || SecurityUtils.currentUserHasRole("SYSTEM_ADMIN");

        model.addAttribute("selectedDate", targetDate);
        if (isAdmin) {
            model.addAttribute("entries", wsrService.getTeamWsr(targetDate, targetDate));
            model.addAttribute("scope", "team");
        } else {
            model.addAttribute("entries", wsrService.getDeveloperWsr(userId, targetDate, targetDate));
            model.addAttribute("scope", "personal");
        }
        return "reports/daily-wsr";
    }

    @GetMapping("/weekly")
    public String weeklyWsr(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                             Model model) {
        LocalDate start = weekStart != null ? weekStart : LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        Long userId = SecurityUtils.currentUserId();
        boolean isAdmin = SecurityUtils.currentUserHasRole("TEAM_LEAD") || SecurityUtils.currentUserHasRole("TECHNICAL_LEAD")
                || SecurityUtils.currentUserHasRole("DASHBOARD_HANDLER") || SecurityUtils.currentUserHasRole("SYSTEM_ADMIN");

        model.addAttribute("weekStart", start);
        model.addAttribute("weekEnd", end);
        if (isAdmin) {
            model.addAttribute("entries", wsrService.getTeamWsr(start, end));
        } else {
            model.addAttribute("entries", wsrService.getDeveloperWsr(userId, start, end));
        }
        return "reports/weekly-wsr";
    }
}

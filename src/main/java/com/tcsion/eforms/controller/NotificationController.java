package com.tcsion.eforms.controller;

import com.tcsion.eforms.dto.response.ApiResponse;
import com.tcsion.eforms.entity.Notification;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String notifications(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Notification> notifications = notificationService.getNotificationsForUser(
                SecurityUtils.currentUserId(), PageRequest.of(page, 20));
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(SecurityUtils.currentUserId()));
        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        notificationService.markAsRead(id, SecurityUtils.currentUserId());
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead() {
        notificationService.markAllAsRead(SecurityUtils.currentUserId());
        return "redirect:/notifications";
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.getUnreadCount(SecurityUtils.currentUserId()));
    }
}

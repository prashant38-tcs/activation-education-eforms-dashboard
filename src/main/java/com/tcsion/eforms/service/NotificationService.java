package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.Notification;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void notify(User recipient, String type, String title, String message, Ticket relatedTicket);
    Page<Notification> getNotificationsForUser(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}

package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.Notification;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.repository.NotificationRepository;
import com.tcsion.eforms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notify(User recipient, String type, String title, String message, Ticket relatedTicket) {
        if (recipient == null) return;
        Notification notification = Notification.builder()
                .recipient(recipient).notificationType(type).title(title)
                .message(message).relatedTicket(relatedTicket).build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(userId) && !n.isRead()) {
                n.setRead(true);
                n.setReadAt(java.time.LocalDateTime.now());
                notificationRepository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}

package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    long countByRecipient_IdAndReadFalse(Long recipientId);

    @Modifying
    @Query("update Notification n set n.read = true, n.readAt = CURRENT_TIMESTAMP where n.recipient.id = :userId and n.read = false")
    int markAllAsReadForUser(@Param("userId") Long userId);
}

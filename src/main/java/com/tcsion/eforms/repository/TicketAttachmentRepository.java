package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicket_IdAndActiveTrueOrderByUploadedAtDesc(Long ticketId);
}

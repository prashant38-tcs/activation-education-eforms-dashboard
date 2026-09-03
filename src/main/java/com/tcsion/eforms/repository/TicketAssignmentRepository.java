package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {
    List<TicketAssignment> findByTicket_IdOrderByAssignedAtDesc(Long ticketId);
    List<TicketAssignment> findByAssignedAtAfterOrderByAssignedAtDesc(LocalDateTime after);
}

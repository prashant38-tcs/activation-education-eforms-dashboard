package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.DatabaseChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DatabaseChangeRepository extends JpaRepository<DatabaseChange, Long> {
    List<DatabaseChange> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
}

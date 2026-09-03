package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {
    List<TicketActivity> findByTicket_IdOrderByActivityDatetimeDesc(Long ticketId);
    Optional<TicketActivity> findFirstByTicket_IdOrderByActivityDatetimeDesc(Long ticketId);

    @Query("select a from TicketActivity a where a.updatedBy.id = :userId and a.activityDatetime between :start and :end order by a.activityDatetime desc")
    List<TicketActivity> findByDeveloperAndDateRange(@Param("userId") Long userId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    @Query("select a from TicketActivity a where a.activityDatetime between :start and :end order by a.activityDatetime desc")
    List<TicketActivity> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select count(a) from TicketActivity a where a.ticket.id = :ticketId and a.newStatus.statusCode = :statusCode")
    long countTicketVisitsToStatus(@Param("ticketId") Long ticketId, @Param("statusCode") String statusCode);
}

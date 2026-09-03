package com.tcsion.eforms.repository;

import com.tcsion.eforms.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumberIgnoreCase(String ticketNumber);
    Optional<Ticket> findByCrmIdIgnoreCase(String crmId);
    boolean existsByTicketNumberIgnoreCase(String ticketNumber);

    Optional<Ticket> findByIdAndAssignedUser_Id(Long id, Long assignedUserId);
    Page<Ticket> findByAssignedUser_IdAndActiveTrue(Long assignedUserId, Pageable pageable);
    List<Ticket> findByAssignedUser_IdAndActiveTrueAndCurrentStatus_OpenTrue(Long assignedUserId);

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true")
    List<Ticket> findAllOpenActive();

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and t.assignedUser.id = :userId")
    List<Ticket> findOpenActiveByDeveloper(@Param("userId") Long userId);

    @Query("select count(t) from Ticket t where t.active = true and t.currentStatus.open = true")
    long countAllOpen();

    @Query("select count(t) from Ticket t where t.active = true and t.createdDate >= :start and t.createdDate < :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select count(t) from Ticket t where t.active = true and t.actualProductionDate = :date")
    long countMovedToProductionOn(@Param("date") LocalDate date);

    @Query("select count(t) from Ticket t where t.active = true and t.currentStatus.statusCode = 'ON_HOLD'")
    long countOnHold();

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and " +
           "(t.lastActivityDate is null or t.lastActivityDate < :threshold)")
    List<Ticket> findTicketsWithoutRecentUpdate(@Param("threshold") LocalDateTime threshold);

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and t.agingDays >= :minDays")
    List<Ticket> findByAgingDaysGreaterThanEqual(@Param("minDays") int minDays);

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and t.slaRiskCategory = :category")
    List<Ticket> findByRiskCategory(@Param("category") String category);

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and " +
           "t.estimatedProductionDate is not null and t.estimatedProductionDate between :today and :nearDate")
    List<Ticket> findApproachingProductionDate(@Param("today") LocalDate today, @Param("nearDate") LocalDate nearDate);

    @Query("select t from Ticket t where t.active = true and t.currentStatus.open = true and " +
           "t.estimatedProductionDate is not null and t.estimatedProductionDate < :today")
    List<Ticket> findOverdueTickets(@Param("today") LocalDate today);

    List<Ticket> findByCustomer_IdAndActiveTrue(Long customerId);
}

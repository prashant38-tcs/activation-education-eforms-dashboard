package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.WsrDailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface WsrDailySnapshotRepository extends JpaRepository<WsrDailySnapshot, Long> {
    List<WsrDailySnapshot> findByDeveloper_IdAndSnapshotDateBetweenOrderBySnapshotDateDesc(
            Long developerId, LocalDate start, LocalDate end);
    List<WsrDailySnapshot> findBySnapshotDateBetweenOrderBySnapshotDateDesc(LocalDate start, LocalDate end);
    boolean existsByActivity_Id(Long activityId);
}

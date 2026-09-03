package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.ImportBatchRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImportBatchRowRepository extends JpaRepository<ImportBatchRow, Long> {
    List<ImportBatchRow> findByBatch_IdOrderByRowNumberAsc(Long batchId);
    List<ImportBatchRow> findByBatch_IdAndRowClassification(Long batchId, String classification);
}

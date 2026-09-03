package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    List<ImportBatch> findAllByOrderByUploadedAtDesc();
    Optional<ImportBatch> findFirstByChecksumOrderByUploadedAtDesc(String checksum);
}

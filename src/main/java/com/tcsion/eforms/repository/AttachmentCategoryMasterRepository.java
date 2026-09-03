package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.AttachmentCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentCategoryMasterRepository extends JpaRepository<AttachmentCategoryMaster, Long> {
    List<AttachmentCategoryMaster> findByActiveTrue();
}

package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.TicketTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketTypeMasterRepository extends JpaRepository<TicketTypeMaster, Long> {
    List<TicketTypeMaster> findByActiveTrue();
    Optional<TicketTypeMaster> findByTypeCode(String typeCode);
}

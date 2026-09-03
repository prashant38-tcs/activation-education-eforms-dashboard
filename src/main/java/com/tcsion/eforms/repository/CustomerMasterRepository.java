package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.CustomerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerMasterRepository extends JpaRepository<CustomerMaster, Long> {
    List<CustomerMaster> findByActiveTrue();
    Optional<CustomerMaster> findByCustomerNameIgnoreCase(String customerName);
    boolean existsByCustomerNameIgnoreCaseAndIdNot(String customerName, Long id);
}

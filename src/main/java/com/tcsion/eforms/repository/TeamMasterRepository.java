package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.TeamMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeamMasterRepository extends JpaRepository<TeamMaster, Long> {
    List<TeamMaster> findByActiveTrue();
    Optional<TeamMaster> findByTeamCode(String teamCode);
}

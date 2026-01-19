package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportsStatusRepository extends JpaRepository<ReportsStatus, Integer> {
	List<ReportsStatus> findByReport_Id(int reportId);
}

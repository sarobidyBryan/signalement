package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportsAssignationProgressRepository extends JpaRepository<ReportsAssignationProgress, Integer> {

	@org.springframework.data.jpa.repository.Query("SELECT SUM(r.treatedArea) FROM ReportsAssignationProgress r WHERE r.reportsAssignation.report.id = :reportId")
	java.math.BigDecimal sumTreatedAreaByReportId(@org.springframework.data.repository.query.Param("reportId") int reportId);
}

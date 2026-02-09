package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportsStatusRepository extends JpaRepository<ReportsStatus, Integer> {
	@Query(value = "SELECT r2.report_id FROM reports_status r2 JOIN status s ON r2.status_id = s.id WHERE r2.registration_date = (SELECT MAX(r3.registration_date) FROM reports_status r3 WHERE r3.report_id = r2.report_id) AND s.status_code = :code", nativeQuery = true)
	List<Integer> findReportIdsWithLatestStatus(@Param("code") String statusCode);

	List<ReportsStatus> findByReport_Id(int reportId);

	// Compter les signalements avec un statut donné (dernier statut uniquement)
	@Query(value = "SELECT COUNT(DISTINCT r2.report_id) FROM reports_status r2 JOIN status s ON r2.status_id = s.id WHERE r2.registration_date = (SELECT MAX(r3.registration_date) FROM reports_status r3 WHERE r3.report_id = r2.report_id) AND s.status_code = :statusCode", nativeQuery = true)
	long countByStatus_StatusCode(@Param("statusCode") String statusCode);
}


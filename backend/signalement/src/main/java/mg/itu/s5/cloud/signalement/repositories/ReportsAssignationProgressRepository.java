package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportsAssignationProgressRepository extends JpaRepository<ReportsAssignationProgress, Integer> {

    @Query("SELECT SUM(r.treatedArea) FROM ReportsAssignationProgress r WHERE r.reportsAssignation.report.id = :reportId")
    java.math.BigDecimal sumTreatedAreaByReportId(@Param("reportId") int reportId);
    
    Optional<ReportsAssignationProgress> findByFirebaseId(String firebaseId);
    
    @Query("SELECT r FROM ReportsAssignationProgress r WHERE r.createdAt > :since OR r.updatedAt > :since")
    List<ReportsAssignationProgress> findModifiedSince(@Param("since") LocalDateTime since);

    List<ReportsAssignationProgress> findByReportsAssignation_Id(int assignationId);
}

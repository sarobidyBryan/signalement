package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ReportsAssignationRepository extends JpaRepository<ReportsAssignation, Integer> {
    List<ReportsAssignation> findByReport_Id(int reportId);
    Optional<ReportsAssignation> findByFirebaseId(String firebaseId);

    @Query("""
        SELECT r FROM ReportsAssignation r WHERE r.report.id = :reportId
          AND (:startDate IS NULL OR r.startDate >= :startDate)
          AND (:budgetMin IS NULL OR r.budget >= :budgetMin)
          AND (:budgetMax IS NULL OR r.budget <= :budgetMax)
        """)
    List<ReportsAssignation> findByReportIdWithFilters(
            @Param("reportId") int reportId,
            @Param("startDate") LocalDate startDate,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax
    );

    @Query("SELECT SUM(r.budget) FROM ReportsAssignation r WHERE r.report.id = :reportId")
    BigDecimal sumBudgetByReportId(@Param("reportId") int reportId);

    @Query("SELECT r FROM ReportsAssignation r WHERE r.createdAt > :since OR r.updatedAt > :since")
    List<ReportsAssignation> findModifiedSince(@Param("since") LocalDateTime since);
}


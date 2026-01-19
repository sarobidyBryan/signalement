package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ReportsAssignationRepository extends JpaRepository<ReportsAssignation, Integer> {
    List<ReportsAssignation> findByReport_Id(int reportId);

    @Query("""
        SELECT r FROM ReportsAssignation r WHERE r.report.id = :reportId 
	    AND (:startDate IS NULL OR r.startDate >= :startDate) 
	    AND (:budgetMin IS NULL OR r.budget >= :budgetMin) 
	    AND (:budgetMax IS NULL OR r.budget <= :budgetMax)")    
            """)
    List<ReportsAssignation> findByReportIdWithFilters(int reportId, LocalDate startDate, BigDecimal budgetMin, BigDecimal budgetMax);

}


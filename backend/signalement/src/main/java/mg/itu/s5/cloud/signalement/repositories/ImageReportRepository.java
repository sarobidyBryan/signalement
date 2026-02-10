package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.ImageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageReportRepository extends JpaRepository<ImageReport, Integer> {
    List<ImageReport> findByReport_IdOrderByCreatedAtDesc(int reportId);
    boolean existsByLienAndReport_Id(String lien, int reportId);
}

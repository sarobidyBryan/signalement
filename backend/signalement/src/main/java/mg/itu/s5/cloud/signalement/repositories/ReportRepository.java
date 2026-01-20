package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    Optional<Report> findByFirebaseId(String firebaseId);
    
    @Query("SELECT r FROM Report r WHERE r.createdAt > :since OR r.updatedAt > :since")
    List<Report> findModifiedSince(@Param("since") LocalDateTime since);
}

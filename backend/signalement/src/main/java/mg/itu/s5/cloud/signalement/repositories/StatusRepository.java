package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByStatusCode(String statusCode);
    
    @Query("SELECT s FROM Status s WHERE s.updatedAt > :lastSync")
    List<Status> findModifiedSince(@Param("lastSync") LocalDateTime lastSync);
}

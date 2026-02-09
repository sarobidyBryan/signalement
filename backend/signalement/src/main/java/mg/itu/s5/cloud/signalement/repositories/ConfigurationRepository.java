package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {
    Optional<Configuration> findByKey(String key);
    Optional<Configuration> findByFirebaseId(String firebaseId);
    
    @Query("SELECT c FROM Configuration c WHERE c.createdAt > :since OR c.updatedAt > :since")
    List<Configuration> findModifiedSince(@Param("since") LocalDateTime since);
}
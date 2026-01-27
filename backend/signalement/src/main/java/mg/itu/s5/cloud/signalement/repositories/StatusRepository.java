package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByStatusCode(String statusCode);
    
    // Note: Status table has no updatedAt field, so findModifiedSince is removed
    // For syncing purposes, use findAll() instead
}

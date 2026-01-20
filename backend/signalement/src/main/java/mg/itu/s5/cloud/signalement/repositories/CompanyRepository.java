package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByFirebaseId(String firebaseId);
    Optional<Company> findByEmail(String email);
    
    @Query("SELECT c FROM Company c WHERE c.createdAt > :since OR c.updatedAt > :since")
    List<Company> findModifiedSince(@Param("since") LocalDateTime since);
}

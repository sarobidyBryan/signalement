package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatusTypeRepository extends JpaRepository<UserStatusType, Integer> {
    Optional<UserStatusType> findByStatusCode(String statusCode);
}
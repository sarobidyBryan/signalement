package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Integer> {
    List<UserToken> findByUserId(Integer userId);
    Optional<UserToken> findByToken(String token);
    
    @Query("SELECT ut FROM UserToken ut WHERE ut.createdAt > :since OR ut.updatedAt > :since")
    List<UserToken> findModifiedSince(@Param("since") LocalDateTime since);
}

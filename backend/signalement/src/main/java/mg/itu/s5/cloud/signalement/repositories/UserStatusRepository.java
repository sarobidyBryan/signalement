package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.entities.UserStatus;
import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Integer> {
    List<UserStatus> findByUser(User user);
    Optional<UserStatus> findByUserAndUserStatusType(User user, UserStatusType userStatusType);
    List<UserStatus> findByUserStatusType(UserStatusType userStatusType);
}
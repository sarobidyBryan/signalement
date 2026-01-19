package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.Role;
import mg.itu.s5.cloud.signalement.entities.RoleAttribution;
import mg.itu.s5.cloud.signalement.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleAttributionRepository extends JpaRepository<RoleAttribution, Integer> {
    List<RoleAttribution> findByUser(User user);
    Optional<RoleAttribution> findByUserAndRole(User user, Role role);
    List<RoleAttribution> findByRole(Role role);
}
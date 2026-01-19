package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Role;
import mg.itu.s5.cloud.signalement.entities.RoleAttribution;
import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.repositories.RoleAttributionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleAttributionService {

    @Autowired
    private RoleAttributionRepository roleAttributionRepository;

    public List<RoleAttribution> getAllRoleAttributions() {
        return roleAttributionRepository.findAll();
    }

    public Optional<RoleAttribution> getRoleAttributionById(int id) {
        return roleAttributionRepository.findById(id);
    }

    public List<RoleAttribution> getRoleAttributionsByUser(User user) {
        return roleAttributionRepository.findByUser(user);
    }

    public Optional<RoleAttribution> getRoleAttributionByUserAndRole(User user, Role role) {
        return roleAttributionRepository.findByUserAndRole(user, role);
    }

    public List<RoleAttribution> getRoleAttributionsByRole(Role role) {
        return roleAttributionRepository.findByRole(role);
    }

    public RoleAttribution saveRoleAttribution(RoleAttribution roleAttribution) {
        return roleAttributionRepository.save(roleAttribution);
    }

    public void deleteRoleAttribution(int id) {
        roleAttributionRepository.deleteById(id);
    }
}
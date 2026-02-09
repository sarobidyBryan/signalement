package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.entities.UserStatus;
import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import mg.itu.s5.cloud.signalement.repositories.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserStatusService {

    @Autowired
    private UserStatusRepository userStatusRepository;

    public List<UserStatus> getAllUserStatuses() {
        return userStatusRepository.findAll();
    }

    public Optional<UserStatus> getUserStatusById(int id) {
        return userStatusRepository.findById(id);
    }

    public List<UserStatus> getUserStatusesByUser(User user) {
        return userStatusRepository.findByUser(user);
    }

    public Optional<UserStatus> getUserStatusByUserAndType(User user, UserStatusType userStatusType) {
        return userStatusRepository.findByUserAndUserStatusType(user, userStatusType);
    }

    public List<UserStatus> getUserStatusesByType(UserStatusType userStatusType) {
        return userStatusRepository.findByUserStatusType(userStatusType);
    }

    public UserStatus saveUserStatus(UserStatus userStatus) {
        return userStatusRepository.save(userStatus);
    }

    public void deleteUserStatus(int id) {
        userStatusRepository.deleteById(id);
    }
}
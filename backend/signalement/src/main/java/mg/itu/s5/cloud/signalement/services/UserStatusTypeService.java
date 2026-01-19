package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import mg.itu.s5.cloud.signalement.repositories.UserStatusTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserStatusTypeService {

    @Autowired
    private UserStatusTypeRepository userStatusTypeRepository;

    public List<UserStatusType> getAllUserStatusTypes() {
        return userStatusTypeRepository.findAll();
    }

    public Optional<UserStatusType> getUserStatusTypeById(int id) {
        return userStatusTypeRepository.findById(id);
    }

    public Optional<UserStatusType> getUserStatusTypeByCode(String statusCode) {
        return userStatusTypeRepository.findByStatusCode(statusCode);
    }

    public UserStatusType saveUserStatusType(UserStatusType userStatusType) {
        return userStatusTypeRepository.save(userStatusType);
    }

    public void deleteUserStatusType(int id) {
        userStatusTypeRepository.deleteById(id);
    }
}
package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.entities.UserStatus;
import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import mg.itu.s5.cloud.signalement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatusService userStatusService;

    @Autowired
    private UserStatusTypeService userStatusTypeService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByFirebaseUid(String firebaseUid) {
        return userRepository.existsByFirebaseUid(firebaseUid);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    // Méthode pour mettre à jour le statut d'un utilisateur et insérer dans user_status
    public User updateUserStatus(int userId, String statusCode) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Optional<UserStatusType> statusTypeOpt = userStatusTypeService.getUserStatusTypeByCode(statusCode);
        if (statusTypeOpt.isEmpty()) {
            throw new RuntimeException("Status type not found: " + statusCode);
        }

        User user = userOpt.get();
        UserStatusType newStatusType = statusTypeOpt.get();

        // Vérifier si le statut a changé
        if (!user.getUserStatusType().getStatusCode().equals(newStatusType.getStatusCode())) {
            // Créer une nouvelle entrée dans user_status
            UserStatus userStatus = new UserStatus();
            userStatus.setUser(user);
            userStatus.setUserStatusType(newStatusType);
            userStatus.setRegistrationDate(LocalDateTime.now());

            userStatusService.saveUserStatus(userStatus);

            // Mettre à jour le statut de l'utilisateur
            user.setUserStatusType(newStatusType);
            return userRepository.save(user);
        }

        return user;
    }

    // Méthode pour mettre à jour les informations de l'utilisateur
    public User updateUser(int userId, String name, String email) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already used by another user");
        }

        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }
}
package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.entities.Role;
import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import mg.itu.s5.cloud.signalement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserStatusTypeService userStatusTypeService;

    @Value("${app.session.timeout.seconds}")
    private int sessionTimeoutSeconds;

    private static final String USER_SESSION_KEY = "currentUser";

    // Inscription d'un nouvel utilisateur
    public User register(String name, String email, String password, String firebaseUid) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        // Récupérer le rôle par défaut (USER)
        Optional<Role> defaultRole = roleService.getRoleByCode("USER");
        if (defaultRole.isEmpty()) {
            throw new RuntimeException("Default role not found");
        }

        // Récupérer le statut par défaut (ACTIVE)
        Optional<UserStatusType> defaultStatus = userStatusTypeService.getUserStatusTypeByCode("ACTIVE");
        if (defaultStatus.isEmpty()) {
            throw new RuntimeException("Default status not found");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // TODO: Hasher le mot de passe
        user.setFirebaseUid(firebaseUid);
        user.setRole(defaultRole.get());
        user.setUserStatusType(defaultStatus.get());

        return userRepository.save(user);
    }

    // Connexion par email/mot de passe
    public boolean login(String email, String password, HttpSession session) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // TODO: Vérifier le mot de passe hashé
            if (password.equals(user.getPassword()) && isUserActive(user)) {
                session.setAttribute(USER_SESSION_KEY, user);
                session.setMaxInactiveInterval(sessionTimeoutSeconds);
                return true;
            }
        }
        return false;
    }

    // Connexion par Firebase UID
    public boolean loginByFirebase(String firebaseUid, HttpSession session) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        if (userOpt.isPresent() && isUserActive(userOpt.get())) {
            User user = userOpt.get();
            session.setAttribute(USER_SESSION_KEY, user);
            session.setMaxInactiveInterval(sessionTimeoutSeconds);
            return true;
        }
        return false;
    }

    // Déconnexion
    public void logout(HttpSession session) {
        session.removeAttribute(USER_SESSION_KEY);
        session.invalidate();
    }

    // Obtenir l'utilisateur actuel depuis la session
    public Optional<User> getCurrentUser(HttpSession session) {
        return Optional.ofNullable((User) session.getAttribute(USER_SESSION_KEY));
    }

    // Vérifier si un utilisateur est connecté
    public boolean isAuthenticated(HttpSession session) {
        return getCurrentUser(session).isPresent();
    }

    // Vérifier si l'utilisateur a un rôle spécifique
    public boolean hasRole(HttpSession session, String roleCode) {
        return getCurrentUser(session)
                .map(user -> user.getRole().getRoleCode().equals(roleCode))
                .orElse(false);
    }

    // Vérifier si l'utilisateur est actif
    private boolean isUserActive(User user) {
        return "ACTIVE".equals(user.getUserStatusType().getStatusCode());
    }

    // Régénérer la session (pour prolonger la durée de vie)
    public void refreshSession(HttpSession session) {
        if (isAuthenticated(session)) {
            session.setMaxInactiveInterval(sessionTimeoutSeconds);
        }
    }
}
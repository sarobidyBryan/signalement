package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authenticationService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getFirebaseUid()
            );
            return ResponseEntity.ok(Map.of("message", "Utilisateur créé avec succès", "userId", user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        boolean success = authenticationService.login(request.getEmail(), request.getPassword(), session);
        if (success) {
            Optional<User> user = authenticationService.getCurrentUser(session);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Connexion réussie");
            response.put("user", Map.of(
                "id", user.get().getId(),
                "name", user.get().getName(),
                "email", user.get().getEmail(),
                "role", user.get().getRole().getRoleCode()
            ));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Email ou mot de passe incorrect"));
        }
    }

    @PostMapping("/login/firebase")
    public ResponseEntity<?> loginByFirebase(@RequestBody FirebaseLoginRequest request, HttpSession session) {
        boolean success = authenticationService.loginByFirebase(request.getFirebaseUid(), session);
        if (success) {
            Optional<User> user = authenticationService.getCurrentUser(session);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Connexion Firebase réussie");
            response.put("user", Map.of(
                "id", user.get().getId(),
                "name", user.get().getName(),
                "email", user.get().getEmail(),
                "role", user.get().getRole().getRoleCode()
            ));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur Firebase non trouvé"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authenticationService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Optional<User> user = authenticationService.getCurrentUser(session);
        if (user.isPresent()) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.get().getId());
            userData.put("name", user.get().getName());
            userData.put("email", user.get().getEmail());
            userData.put("role", user.get().getRole().getRoleCode());
            return ResponseEntity.ok(userData);
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non authentifié"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshSession(HttpSession session) {
        if (authenticationService.isAuthenticated(session)) {
            authenticationService.refreshSession(session);
            return ResponseEntity.ok(Map.of("message", "Session prolongée"));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non authentifié"));
        }
    }

    // Classes de requête
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String firebaseUid;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirebaseUid() { return firebaseUid; }
        public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class FirebaseLoginRequest {
        private String firebaseUid;

        // Getters and setters
        public String getFirebaseUid() { return firebaseUid; }
        public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    }
}
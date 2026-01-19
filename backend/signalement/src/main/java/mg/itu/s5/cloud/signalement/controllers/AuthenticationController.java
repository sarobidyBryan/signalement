package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
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
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = authenticationService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getFirebaseUid()
            );
            Map<String, Object> data = new HashMap<>();
            data.put("message", "User created successfully");
            data.put("userId", user.getId());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.USER_ALREADY_EXISTS, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        boolean success = authenticationService.login(request.getEmail(), request.getPassword(), session);
        if (success) {
            Optional<User> user = authenticationService.getCurrentUser(session);
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Login successful");
            data.put("user", Map.of(
                "id", user.get().getId(),
                "name", user.get().getName(),
                "email", user.get().getEmail(),
                "role", user.get().getRole().getRoleCode()
            ));
            return ResponseEntity.ok(ApiResponse.success(data));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_CREDENTIALS, "Invalid email or password"));
        }
    }

    @PostMapping("/login/firebase")
    public ResponseEntity<ApiResponse> loginByFirebase(@RequestBody FirebaseLoginRequest request, HttpSession session) {
        boolean success = authenticationService.loginByFirebase(request.getFirebaseUid(), session);
        if (success) {
            Optional<User> user = authenticationService.getCurrentUser(session);
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Firebase login successful");
            data.put("user", Map.of(
                "id", user.get().getId(),
                "name", user.get().getName(),
                "email", user.get().getEmail(),
                "role", user.get().getRole().getRoleCode()
            ));
            return ResponseEntity.ok(ApiResponse.success(data));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Firebase user not found"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        authenticationService.logout(session);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse> getCurrentUser(HttpSession session) {
        Optional<User> user = authenticationService.getCurrentUser(session);
        if (user.isPresent()) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.get().getId());
            userData.put("name", user.get().getName());
            userData.put("email", user.get().getEmail());
            userData.put("role", user.get().getRole().getRoleCode());
            return ResponseEntity.ok(ApiResponse.success(userData));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshSession(HttpSession session) {
        if (authenticationService.isAuthenticated(session)) {
            authenticationService.refreshSession(session);
            return ResponseEntity.ok(ApiResponse.success("Session refreshed"));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
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
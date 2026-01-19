package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.services.UserService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("users", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable int id, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "User not found"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable int id, @RequestBody UpdateUserRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        try {
            User updatedUser = userService.updateUser(id, request.getName(), request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(@PathVariable int id, @RequestBody UpdateStatusRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            User updatedUser = userService.updateUserStatus(id, request.getStatusCode(), request.getRegistrationDate());
            Map<String, Object> data = new HashMap<>();
            data.put("message", "User status updated successfully");
            data.put("user", updatedUser);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable int id, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getCurrentUserProfile(HttpSession session) {
        Optional<User> user = authenticationService.getCurrentUser(session);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get()));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }
    }

    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse> getUsersBlocked(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "10") int perPage,
            HttpSession session) {

        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        List<User> blocked = userService.getAllUserBlocked("SUSPENDED");
        List<Map<String, Object>> usersDto = new java.util.ArrayList<>();
        for (User u : blocked) {
            Map<String, Object> uMap = new HashMap<>();
            uMap.put("id", u.getId());
            uMap.put("name", u.getName());
            uMap.put("email", u.getEmail());
            uMap.put("role", u.getRole() != null ? u.getRole().getRoleCode() : null);
            uMap.put("statusCode", u.getUserStatusType() != null ? u.getUserStatusType().getStatusCode() : null);
            usersDto.add(uMap);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("users", usersDto);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // Classes de requête
    public static class UpdateUserRequest {
        private String name;
        private String email;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class UpdateStatusRequest {
        private String statusCode;
        private LocalDateTime registrationDate;

        // Getters and setters
        public void setRegistrationDate(LocalDateTime registrationDate) {
            this.registrationDate = registrationDate;
        }
        public LocalDateTime getRegistrationDate() {
            return registrationDate;
        }
        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    }
}
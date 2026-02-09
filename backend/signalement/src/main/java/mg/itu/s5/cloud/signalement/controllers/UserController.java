package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Role;
import mg.itu.s5.cloud.signalement.entities.User;
import mg.itu.s5.cloud.signalement.entities.UserStatusType;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.services.RoleService;
import mg.itu.s5.cloud.signalement.services.UserService;
import mg.itu.s5.cloud.signalement.services.UserStatusTypeService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "API for managing users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserStatusTypeService userStatusTypeService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users (MANAGER role required)")
    public ResponseEntity<ApiResponse> getAllUsers(HttpSession session) {
        // if (!authenticationService.isAuthenticated(session)) {
        //     return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        // }

        // if (!authenticationService.hasRole(session, "MANAGER")) {
        //     return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        // }

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("users", users));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request, HttpSession session) {
        try {
            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Email already exists"));
            }

            // Get role
            Optional<Role> roleOpt = roleService.getRoleByCode(request.getRoleCode());
            if (roleOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Role not found: " + request.getRoleCode()));
            }

            // Get status type (default to ACTIVE)
            String statusCode = request.getStatusCode() != null ? request.getStatusCode() : "ACTIVE";
            Optional<UserStatusType> statusOpt = userStatusTypeService.getUserStatusTypeByCode(statusCode);
            if (statusOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Status not found: " + statusCode));
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setRole(roleOpt.get());
            user.setUserStatusType(statusOpt.get());

            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(ApiResponse.success(savedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
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
    @Operation(summary = "Update user", description = "Updates user information (name, email, role, status)")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable int id, @RequestBody UpdateUserRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "User not found"));
            }

            User user = userOpt.get();
            
            if (request.getName() != null) {
                user.setName(request.getName());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getRoleCode() != null) {
                Optional<Role> roleOpt = roleService.getRoleByCode(request.getRoleCode());
                if (roleOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Role not found: " + request.getRoleCode()));
                }
                user.setRole(roleOpt.get());
            }
            if (request.getStatusCode() != null) {
                Optional<UserStatusType> statusOpt = userStatusTypeService.getUserStatusTypeByCode(request.getStatusCode());
                if (statusOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Status not found: " + request.getStatusCode()));
                }
                user.setUserStatusType(statusOpt.get());
            }

            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Updates the status of a user (MANAGER role required)")
    public ResponseEntity<ApiResponse> updateUserStatus(@PathVariable int id, @RequestBody UpdateStatusRequest request, HttpSession session) {
        // if (!authenticationService.isAuthenticated(session)) {
        //     return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        // }

        // if (!authenticationService.hasRole(session, "MANAGER")) {
        //     return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        // }

        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "User not found"));
            }
            User updatedUser = userService.updateUserStatus(id, request.getStatusCode(), userOpt.get().getCreatedAt());
            Map<String, Object> data = new HashMap<>();
            data.put("message", "User status updated successfully");
            data.put("user", updatedUser);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user (MANAGER role required)")
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
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    public ResponseEntity<ApiResponse> getCurrentUserProfile(HttpSession session) {
        Optional<User> user = authenticationService.getCurrentUser(session);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get()));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Retrieves a list of all available roles")
    public ResponseEntity<ApiResponse> getAllRoles(HttpSession session) {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("roles", roles));
    }

    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse> getUsersBlocked(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "10") int perPage,
            HttpSession session) {

        // if (!authenticationService.isAuthenticated(session)) {
        //     return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        // }

        // if (!authenticationService.hasRole(session, "MANAGER")) {
        //     return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        // }

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
        private String roleCode;
        private String statusCode;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    }

    public static class UpdateStatusRequest {
        private String statusCode;

        // Getters and setters
        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    }

    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private String roleCode;
        private String statusCode;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    }
}
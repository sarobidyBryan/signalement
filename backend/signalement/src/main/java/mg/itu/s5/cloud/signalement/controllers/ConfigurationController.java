package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Configuration;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.services.ConfigurationService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/configurations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllConfigurations(HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        List<Configuration> configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success("configurations", configurations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getConfigurationById(@PathVariable int id, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<Configuration> config = configurationService.getConfigurationById(id);
        if (config.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(config.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.CONFIGURATION_NOT_FOUND, "Configuration not found"));
        }
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ApiResponse> getConfigurationByKey(@PathVariable String key, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<Configuration> config = configurationService.getConfigurationByKey(key);
        if (config.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(config.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.CONFIGURATION_NOT_FOUND, "Configuration not found"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createConfiguration(@RequestBody ConfigurationRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            Configuration config = configurationService.createOrUpdateConfiguration(
                request.getKey(),
                request.getValue(),
                request.getType()
            );
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateConfiguration(@PathVariable int id, @RequestBody ConfigurationRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            Optional<Configuration> existingConfig = configurationService.getConfigurationById(id);
            if (existingConfig.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.CONFIGURATION_NOT_FOUND, "Configuration not found"));
            }

            Configuration config = existingConfig.get();
            config.setKey(request.getKey());
            config.setValue(request.getValue());
            config.setType(request.getType());

            Configuration savedConfig = configurationService.saveConfiguration(config);
            return ResponseEntity.ok(ApiResponse.success(savedConfig));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @PutMapping("/key/{key}")
    public ResponseEntity<ApiResponse> updateConfigurationByKey(@PathVariable String key, @RequestBody ConfigurationValueRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            Configuration config = configurationService.createOrUpdateConfiguration(
                key,
                request.getValue(),
                request.getType() != null ? request.getType() : "string"
            );
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteConfiguration(@PathVariable int id, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            configurationService.deleteConfiguration(id);
            return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/key/{key}")
    public ResponseEntity<ApiResponse> deleteConfigurationByKey(@PathVariable String key, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            configurationService.deleteConfigurationByKey(key);
            return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // Classes de requête
    public static class ConfigurationRequest {
        private String key;
        private String value;
        private String type;

        // Getters and setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class ConfigurationValueRequest {
        private String value;
        private String type;

        // Getters and setters
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Configuration;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.services.ConfigurationService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/configurations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Configuration Management", description = "API for managing application configurations")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    @Operation(summary = "Get all configurations", description = "Retrieves all application configurations")
    public ResponseEntity<ApiResponse> getAllConfigurations(HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        List<Configuration> configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success("configurations", configurations));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get configuration by key", description = "Retrieves a specific configuration by its key")
    public ResponseEntity<ApiResponse> getConfigurationByKey(@PathVariable String key, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<Configuration> configuration = configurationService.getConfigurationByKey(key);
        if (configuration.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(configuration.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.CONFIGURATION_NOT_FOUND, "Configuration not found"));
        }
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update configuration", description = "Updates the value of a configuration (MANAGER role required)")
    public ResponseEntity<ApiResponse> updateConfiguration(@PathVariable String key, @RequestBody UpdateConfigurationRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            Configuration updatedConfig = configurationService.createOrUpdateConfiguration(key, request.getValue(), request.getType() != null ? request.getType() : "string");
            return ResponseEntity.ok(ApiResponse.success(updatedConfig));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create configuration", description = "Creates a new configuration (MANAGER role required)")
    public ResponseEntity<ApiResponse> createConfiguration(@RequestBody CreateConfigurationRequest request, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            Configuration config = configurationService.createOrUpdateConfiguration(request.getKey(), request.getValue(), request.getType());
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Delete configuration", description = "Deletes a configuration by key (MANAGER role required)")
    public ResponseEntity<ApiResponse> deleteConfiguration(@PathVariable String key, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        if (!authenticationService.hasRole(session, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(ApiResponse.ErrorCodes.FORBIDDEN, "Access denied - MANAGER role required"));
        }

        try {
            configurationService.deleteConfigurationByKey(key);
            return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, e.getMessage()));
        }
    }

    // Classes de requête
    public static class UpdateConfigurationRequest {
        private String value;
        private String type;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class CreateConfigurationRequest {
        private String key;
        private String value;
        private String type;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
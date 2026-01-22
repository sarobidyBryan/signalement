package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Status;
import mg.itu.s5.cloud.signalement.services.AuthenticationService;
import mg.itu.s5.cloud.signalement.services.StatusService;
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
@RequestMapping("/api/statuses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Status Management", description = "API for managing report statuses")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    @Operation(summary = "Get all statuses", description = "Retrieves all report statuses")
    public ResponseEntity<ApiResponse> getAllStatuses(HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        List<Status> statuses = statusService.getAllStatuses();
        return ResponseEntity.ok(ApiResponse.success("statuses", statuses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get status by ID", description = "Retrieves a specific status by its ID")
    public ResponseEntity<ApiResponse> getStatusById(@PathVariable int id, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<Status> status = statusService.getStatusById(id);
        if (status.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(status.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.NOT_FOUND, "Status not found"));
        }
    }

    @GetMapping("/code/{statusCode}")
    @Operation(summary = "Get status by status code", description = "Retrieves a specific status by its status code")
    public ResponseEntity<ApiResponse> getStatusByStatusCode(@PathVariable String statusCode, HttpSession session) {
        if (!authenticationService.isAuthenticated(session)) {
            return ResponseEntity.status(401).body(ApiResponse.error(ApiResponse.ErrorCodes.UNAUTHORIZED, "User not authenticated"));
        }

        Optional<Status> status = statusService.getStatusByStatusCode(statusCode);
        if (status.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(status.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.NOT_FOUND, "Status not found"));
        }
    }
}
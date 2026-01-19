package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationProgressService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports/assignation-progress")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Reports Assignation Progress Management", description = "API for managing progress on report assignments")
public class ReportsAssignationProgressController {

    @Autowired
    private ReportsAssignationProgressService service;

    @GetMapping
    @Operation(summary = "Get all progress entries", description = "Retrieves a list of all assignation progress entries with percentage")
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<Map<String, Object>> result = service.getAllWithPercentage();
            return ResponseEntity.ok(ApiResponse.success("progress", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la récupération des progrès"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get progress by ID", description = "Retrieves a specific progress entry by its ID with percentage")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        try {
            return service.getByIdWithPercentage(id)
                    .map(map -> ResponseEntity.ok(ApiResponse.success(map)))
                    .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Progrès non trouvé")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la récupération du progrès"));
        }
    }

    @PostMapping
    @Operation(summary = "Create progress entry", description = "Creates a new progress entry for an assignation (validates surface limits)")
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsAssignationProgress p) {
        try {
            Map<String, Object> map = service.saveWithPercentage(p);
            return ResponseEntity.ok(ApiResponse.success(map));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_DATA, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la création du progrès"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete progress entry", description = "Deletes a progress entry by its ID")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        try {
            if (!service.getById(id).isPresent()) {
                return ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Progrès non trouvé"));
            }
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Progrès supprimé"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la suppression du progrès"));
        }
    }
}

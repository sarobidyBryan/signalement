package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/reports/assignations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Reports Assignation Management", description = "API for managing report assignments to companies")
public class ReportsAssignationController {

    @Autowired
    private ReportsAssignationService service;

    @GetMapping
    @Operation(summary = "Get all assignations", description = "Retrieves a list of all report assignations")
    public ResponseEntity<ApiResponse> getAll() {
        List<ReportsAssignation> all = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("assignations", all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignation by ID", description = "Retrieves a specific assignation by its ID")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return service.getById(id)
                .map(a -> ResponseEntity.ok(ApiResponse.success(a)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Assignation not found")));
    }

    @PostMapping
    @Operation(summary = "Create assignation", description = "Creates a new report assignation")
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsAssignation a) {
        ReportsAssignation saved = service.save(a);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete assignation", description = "Deletes an assignation by its ID")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Assignation deleted"));
    }
}

package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import mg.itu.s5.cloud.signalement.services.ReportsStatusService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/reports/status")
@Tag(name = "Reports Status Management", description = "API for managing report status history")
public class ReportsStatusController {

    @Autowired
    private ReportsStatusService service;

    @GetMapping
    @Operation(summary = "Get all status entries", description = "Retrieves a list of all report status entries")
    public ResponseEntity<ApiResponse> getAll() {
        List<ReportsStatus> all = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("reports_status", all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get status by ID", description = "Retrieves a specific status entry by its ID")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return service.getById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "ReportsStatus not found")));
    }

    @PostMapping
    @Operation(summary = "Create status entry", description = "Creates a new report status entry")
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsStatus r) {
        ReportsStatus saved = service.save(r);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete status entry", description = "Deletes a status entry by its ID")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ReportsStatus deleted"));
    }
}

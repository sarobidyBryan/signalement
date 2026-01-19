package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import mg.itu.s5.cloud.signalement.services.ReportsStatusService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/status")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportsStatusController {

    @Autowired
    private ReportsStatusService service;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<ReportsStatus> all = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("reports_status", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return service.getById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "ReportsStatus not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsStatus r) {
        ReportsStatus saved = service.save(r);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ReportsStatus deleted"));
    }
}

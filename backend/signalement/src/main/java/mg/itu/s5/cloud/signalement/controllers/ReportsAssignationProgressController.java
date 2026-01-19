package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationProgressService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/assignation-progress")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportsAssignationProgressController {

    @Autowired
    private ReportsAssignationProgressService service;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<ReportsAssignationProgress> all = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("progress", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return service.getById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Progress not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsAssignationProgress p) {
        ReportsAssignationProgress saved = service.save(p);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Progress deleted"));
    }
}

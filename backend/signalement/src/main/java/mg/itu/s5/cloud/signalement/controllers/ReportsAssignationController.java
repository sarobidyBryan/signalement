package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/assignations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportsAssignationController {

    @Autowired
    private ReportsAssignationService service;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<ReportsAssignation> all = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("assignations", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return service.getById(id)
                .map(a -> ResponseEntity.ok(ApiResponse.success(a)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Assignation not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody ReportsAssignation a) {
        ReportsAssignation saved = service.save(a);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Assignation deleted"));
    }
}

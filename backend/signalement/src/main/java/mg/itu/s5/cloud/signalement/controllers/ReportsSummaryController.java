package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.services.ReportsSummaryService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports/summary")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportsSummaryController {

    @Autowired
    private ReportsSummaryService summaryService;

    @GetMapping("/in-progress")
    public ResponseEntity<ApiResponse> inProgress() {
        Map<String, Object> res = summaryService.getSummaryForStatus("IN_PROGRESS");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/verified")
    public ResponseEntity<ApiResponse> verified() {
        Map<String, Object> res = summaryService.getSummaryForStatus("VERIFIED");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse> completed() {
        Map<String, Object> res = summaryService.getSummaryForStatus("COMPLETED");
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}

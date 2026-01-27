package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.services.ReportsSummaryService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/reports/summary")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Reports Summary", description = "API for getting summary statistics of reports by status")
public class ReportsSummaryController {

    @Autowired
    private ReportsSummaryService summaryService;

    @GetMapping("/in-progress")
    @Operation(summary = "Get in-progress reports summary", description = "Retrieves summary statistics for reports currently in progress")
    public ResponseEntity<ApiResponse> inProgress() {
        Map<String, Object> res = summaryService.getSummaryForStatus("IN_PROGRESS");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/verified")
    @Operation(summary = "Get verified reports summary", description = "Retrieves summary statistics for verified and completed reports")
    public ResponseEntity<ApiResponse> verified() {
        Map<String, Object> res = summaryService.getSummaryForStatus("VERIFIED");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse> completed() {
        Map<String, Object> res = summaryService.getSummaryForStatus("COMPLETED");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/global")
    @Operation(summary = "Get global summary", description = "Retrieves summary statistics for all reports regardless of status")
    public ResponseEntity<ApiResponse> global() {
        Map<String, Object> res = summaryService.getGlobalSummary();
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}

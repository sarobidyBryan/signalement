package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.services.DashboardService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Dashboard", description = "API for dashboard statistics and overview")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves overall statistics for the dashboard")
    public ResponseEntity<ApiResponse> getStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("stats", stats));
    }

    @GetMapping("/recent-reports")
    @Operation(summary = "Get recent reports", description = "Retrieves the most recent reports")
    public ResponseEntity<ApiResponse> getRecentReports(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> reports = dashboardService.getRecentReports(limit);
        return ResponseEntity.ok(ApiResponse.success("reports", reports));
    }

    @GetMapping("/reports-by-status")
    @Operation(summary = "Get reports count by status", description = "Retrieves the count of reports grouped by status")
    public ResponseEntity<ApiResponse> getReportsByStatus() {
        Map<String, Long> statusCounts = dashboardService.getReportsByStatus();
        return ResponseEntity.ok(ApiResponse.success("reportsByStatus", statusCounts));
    }

    @GetMapping("/overview")
    @Operation(summary = "Get complete dashboard overview", description = "Retrieves all dashboard data including stats, recent reports, and reports by status")
    public ResponseEntity<ApiResponse> getDashboardOverview() {
        Map<String, Object> overview = dashboardService.getDetailedDashboard();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
}

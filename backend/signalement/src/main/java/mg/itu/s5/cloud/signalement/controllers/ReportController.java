package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import mg.itu.s5.cloud.signalement.services.ReportService;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationService;
import mg.itu.s5.cloud.signalement.services.ReportsStatusService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportsAssignationService reportsAssignationService;
    @Autowired
    private ReportsStatusService reportsStatusService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<Report> all = reportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("reports", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return reportService.getReportById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Report not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Report r) {
        Report saved = reportService.saveReport(r);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted"));
    }

    // Infos sur le signalement
    @GetMapping("/{id}/assignations")
        public ResponseEntity<ApiResponse> getAssignationsForReport(
                @PathVariable int id,
                @RequestParam(value = "startDate", required = false) String startDateStr,
                @RequestParam(value = "budgetMin", required = false) String budgetMinStr,
                @RequestParam(value = "budgetMax", required = false) String budgetMaxStr
        ) {
            LocalDate startDate = null;
            BigDecimal budgetMin = null;
            BigDecimal budgetMax = null;
            try {
                if (startDateStr != null && !startDateStr.isBlank()) startDate = LocalDate.parse(startDateStr);
                if (budgetMinStr != null && !budgetMinStr.isBlank()) budgetMin = new BigDecimal(budgetMinStr);
                if (budgetMaxStr != null && !budgetMaxStr.isBlank()) budgetMax = new BigDecimal(budgetMaxStr);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_REQUEST, "Invalid filter format"));
            }

            List<ReportsAssignation> list;
            if (startDate != null) {
                list = reportsAssignationService.findByReportIdWithFilters(id, startDate, null, null);
            } else {
                list = reportsAssignationService.findByReportIdWithFilters(id, null, budgetMin, budgetMax);
            }
            return ResponseEntity.ok(ApiResponse.success("assignations", list));
        }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse> getStatusForReport(@PathVariable int id) {
        List<ReportsStatus> list = reportsStatusService.findByReportId(id);
        return ResponseEntity.ok(ApiResponse.success("statuses", list));
    }

    public static class ReportStatusRequest {
        public int statusId;
        public LocalDateTime registrationDate;
        public int getStatusId() { return statusId; }
        public void setStatusId(int statusId) { this.statusId = statusId; }
        public LocalDateTime getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    }

    // Statut du signalement
    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse> addStatusToReport(@PathVariable int id, @RequestBody ReportStatusRequest req) {
        LocalDateTime reg = req.getRegistrationDate() != null ? req.getRegistrationDate() : LocalDateTime.now();
        ReportsStatus saved = reportsStatusService.addStatusToReport(id, req.getStatusId(), reg);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}

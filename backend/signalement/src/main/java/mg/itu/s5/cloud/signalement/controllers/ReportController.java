package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.dto.ReportDetailDto;
import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import mg.itu.s5.cloud.signalement.services.ReportService;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationProgressService;
import mg.itu.s5.cloud.signalement.services.ReportsAssignationService;
import mg.itu.s5.cloud.signalement.services.ReportsStatusService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Reports Management", description = "API for managing road reports")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportsAssignationService reportsAssignationService;
    @Autowired
    private ReportsStatusService reportsStatusService;
    @Autowired
    private ReportsAssignationProgressService reportsAssignationProgressService;

    @GetMapping
    @Operation(summary = "Get all reports", description = "Retrieves a list of reports, optionally filtered by surface, statut, utilisateur et date")
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam(value = "areaMin", required = false) BigDecimal areaMin,
            @RequestParam(value = "areaMax", required = false) BigDecimal areaMax,
            @RequestParam(value = "statusCode", required = false) String statusCode,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "reportDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDateFrom,
            @RequestParam(value = "reportDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDateTo
    ) {
        List<Report> reports = reportService.searchReports(areaMin, areaMax, statusCode, userId, reportDateFrom, reportDateTo);
        return ResponseEntity.ok(ApiResponse.success("reports", reports));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID", description = "Retrieves a specific report by its ID")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return reportService.getReportById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Report not found")));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Get report detail", description = "Retrieves the report with its assignations, progress history and aggregates")
    public ResponseEntity<ApiResponse> getDetail(@PathVariable int id) {
        return reportService.getReportById(id)
                .map(report -> {
                    List<ReportsAssignation> assignations = reportsAssignationService.findByReportId(id);
                    List<Map<String, Object>> progressEntries = reportsAssignationProgressService.getByReportIdWithPercentage(id);
                    BigDecimal treatedArea = reportsAssignationProgressService.sumTreatedAreaByReportId(id);
                    BigDecimal totalArea = report.getArea() != null ? report.getArea() : BigDecimal.ZERO;
                    BigDecimal progressPercentage = BigDecimal.ZERO;
                    if (totalArea.compareTo(BigDecimal.ZERO) > 0) {
                        progressPercentage = treatedArea.divide(totalArea, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                        if (progressPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                            progressPercentage = BigDecimal.valueOf(100);
                        }
                    }
                    ReportDetailDto detail = new ReportDetailDto();
                    detail.setReport(report);
                    detail.setAssignations(assignations);
                    detail.setProgressEntries(progressEntries);
                    detail.setTreatedArea(treatedArea);
                    detail.setTotalArea(totalArea);
                    detail.setProgressPercentage(progressPercentage);
                    return ResponseEntity.ok(ApiResponse.success("reportDetail", detail));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Report not found")));
    }

    @PostMapping
    @Operation(summary = "Create a new report", description = "Creates a new road report")
    public ResponseEntity<ApiResponse> create(@RequestBody Report r) {
        Report saved = reportService.saveReport(r);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a report", description = "Updates an existing report")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @RequestBody Report reportData) {
        try {
            Report updated = reportService.updateReport(id, reportData);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_DATA, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la mise à jour du signalement"));
        }
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
    @Operation(summary = "Update report status", description = "Updates the report status and creates a history entry")
    public ResponseEntity<ApiResponse> addStatusToReport(@PathVariable int id, @RequestBody ReportStatusRequest req) {
        try {
            LocalDateTime reg = req.getRegistrationDate() != null ? req.getRegistrationDate() : LocalDateTime.now();
            ReportsStatus saved = reportsStatusService.addStatusToReport(id, req.getStatusId(), reg);
            return ResponseEntity.ok(ApiResponse.success(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ApiResponse.ErrorCodes.INVALID_DATA, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Erreur lors de la mise à jour du statut"));
        }
    }
}

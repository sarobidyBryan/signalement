package mg.itu.s5.cloud.signalement.dto;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportDetailDto {
    private Report report;
    private List<ReportsAssignation> assignations = new ArrayList<>();
    private List<Map<String, Object>> progressEntries = new ArrayList<>();
    private BigDecimal treatedArea = BigDecimal.ZERO;
    private BigDecimal totalArea = BigDecimal.ZERO;
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public List<ReportsAssignation> getAssignations() { return assignations; }
    public void setAssignations(List<ReportsAssignation> assignations) { this.assignations = assignations; }

    public List<Map<String, Object>> getProgressEntries() { return progressEntries; }
    public void setProgressEntries(List<Map<String, Object>> progressEntries) { this.progressEntries = progressEntries; }

    public BigDecimal getTreatedArea() { return treatedArea; }
    public void setTreatedArea(BigDecimal treatedArea) { this.treatedArea = treatedArea; }

    public BigDecimal getTotalArea() { return totalArea; }
    public void setTotalArea(BigDecimal totalArea) { this.totalArea = totalArea; }

    public BigDecimal getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(BigDecimal progressPercentage) { this.progressPercentage = progressPercentage; }
}

package mg.itu.s5.cloud.signalement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les statistiques de délai d'un report
 */
public class ReportDelayStat {
    private int reportId;
    private String description;
    private BigDecimal area;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // Company info
    private Integer companyId;
    private String companyName;
    
    // Status info
    private String currentStatus;
    private String statusLabel;
    
    // Progress info
    private BigDecimal treatedArea;
    private double progressPercentage;
    
    // Delay info (in days)
    private Double delayInDays;
    private LocalDateTime startDate;     // Date du premier progress (treatedArea > 0)
    private LocalDateTime endDate;       // Date du dernier progress (si terminé) ou date actuelle (si en cours)
    private boolean isCompleted;
    
    public ReportDelayStat() {}

    public ReportDelayStat(int reportId, String description, BigDecimal area, BigDecimal latitude, BigDecimal longitude,
                           Integer companyId, String companyName, String currentStatus, String statusLabel,
                           BigDecimal treatedArea, double progressPercentage, Double delayInDays,
                           LocalDateTime startDate, LocalDateTime endDate, boolean isCompleted) {
        this.reportId = reportId;
        this.description = description;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.companyId = companyId;
        this.companyName = companyName;
        this.currentStatus = currentStatus;
        this.statusLabel = statusLabel;
        this.treatedArea = treatedArea;
        this.progressPercentage = progressPercentage;
        this.delayInDays = delayInDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public BigDecimal getTreatedArea() { return treatedArea; }
    public void setTreatedArea(BigDecimal treatedArea) { this.treatedArea = treatedArea; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public Double getDelayInDays() { return delayInDays; }
    public void setDelayInDays(Double delayInDays) { this.delayInDays = delayInDays; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}

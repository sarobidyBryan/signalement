package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reports_assignation")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ReportsAssignation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(precision = 15, scale = 2)
    private BigDecimal budget;

    private LocalDate startDate;
    private LocalDate deadline;

    @JsonProperty("firebaseId")
    @Column(name = "firebase_id", length = 100)
    private String firebaseId;

    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    public ReportsAssignation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


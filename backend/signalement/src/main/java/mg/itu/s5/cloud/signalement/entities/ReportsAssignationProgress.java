package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports_assignation_progress")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ReportsAssignationProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_assignation_id", nullable = false)
    private ReportsAssignation reportsAssignation;

    @Column(precision = 10, scale = 2)
    private BigDecimal treatedArea;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime registrationDate;

    public ReportsAssignationProgress() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ReportsAssignation getReportsAssignation() { return reportsAssignation; }
    public void setReportsAssignation(ReportsAssignation reportsAssignation) { this.reportsAssignation = reportsAssignation; }

    public BigDecimal getTreatedArea() { return treatedArea; }
    public void setTreatedArea(BigDecimal treatedArea) { this.treatedArea = treatedArea; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
}

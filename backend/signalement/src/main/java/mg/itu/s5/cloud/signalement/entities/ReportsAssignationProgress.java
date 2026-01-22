package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("firebaseId")
    @Column(name = "firebase_id", length = 100)
    private String firebaseId;

    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

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

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

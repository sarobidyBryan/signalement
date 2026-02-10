package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_report")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ImageReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "lien", length = 1000, nullable = false)
    private String lien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ImageReport() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

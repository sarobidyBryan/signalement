package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "status")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonProperty("statusCode")
    @Column(name = "status_code", length = 20, unique = true)
    private String statusCode;

    private String label;

    @JsonProperty("firebaseId")
    @Column(name = "firebase_id", length = 100)
    private String firebaseId;

    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    public Status() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

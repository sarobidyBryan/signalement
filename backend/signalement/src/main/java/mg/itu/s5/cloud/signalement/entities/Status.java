package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "status")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "status_code", length = 20, unique = true)
    private String statusCode;

    private String label;

    public Status() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}

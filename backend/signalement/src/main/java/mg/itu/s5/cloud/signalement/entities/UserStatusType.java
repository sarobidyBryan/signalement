package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "user_status_types")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class UserStatusType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonProperty("statusCode")
    @Column(name = "status_code", length = 20, unique = true)
    private String statusCode;

    @Column(name = "label", length = 100)
    private String label;

    public UserStatusType() {}

    public UserStatusType(String statusCode, String label) {
        this.statusCode = statusCode;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_code", length = 20)
    private String roleCode;

    @Column(name = "label", length = 100)
    private String label;

    public Role() {}

    public Role(String roleCode, String label) {
        this.roleCode = roleCode;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
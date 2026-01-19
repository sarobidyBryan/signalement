package mg.itu.s5.cloud.signalement.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_attributions")
public class RoleAttribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "attribution_date")
    private LocalDateTime attributionDate;

    public RoleAttribution() {}

    public RoleAttribution(User user, Role role, LocalDateTime attributionDate) {
        this.user = user;
        this.role = role;
        this.attributionDate = attributionDate;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getAttributionDate() {
        return attributionDate;
    }

    public void setAttributionDate(LocalDateTime attributionDate) {
        this.attributionDate = attributionDate;
    }
}
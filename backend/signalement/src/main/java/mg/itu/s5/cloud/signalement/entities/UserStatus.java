package mg.itu.s5.cloud.signalement.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_status")
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_status_type", nullable = false)
    private UserStatusType userStatusType;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    public UserStatus() {}

    public UserStatus(User user, UserStatusType userStatusType, LocalDateTime registrationDate) {
        this.user = user;
        this.userStatusType = userStatusType;
        this.registrationDate = registrationDate;
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

    public UserStatusType getUserStatusType() {
        return userStatusType;
    }

    public void setUserStatusType(UserStatusType userStatusType) {
        this.userStatusType = userStatusType;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
}
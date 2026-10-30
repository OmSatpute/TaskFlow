package com.taskflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_app_users_email", columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 190)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private DashboardProfile profile;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AppUser() { }
    public AppUser(String name, String email, String passwordHash, DashboardProfile profile) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.profile = profile;
    }
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public DashboardProfile getProfile() { return profile; }
}

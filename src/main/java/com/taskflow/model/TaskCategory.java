package com.taskflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "task_categories", uniqueConstraints =
        @UniqueConstraint(name = "uk_task_category_domain_name", columnNames = {"domain", "name"}))
public class TaskCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String domain;

    @Column(nullable = false, length = 80)
    private String name;

    protected TaskCategory() { }

    public TaskCategory(String domain, String name) {
        this.domain = domain;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

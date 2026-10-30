package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_student")
public class StudentTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.STUDENT; } }

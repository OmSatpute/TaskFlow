package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_teacher")
public class TeacherTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.TEACHER; } }

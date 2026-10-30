package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_personal")
public class PersonalTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.PERSONAL; } }

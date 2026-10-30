package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_freelancer")
public class FreelancerTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.FREELANCER; } }

package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_office_worker")
public class OfficeWorkerTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.OFFICE_WORKER; } }

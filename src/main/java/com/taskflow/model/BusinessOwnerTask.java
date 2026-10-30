package com.taskflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity @Table(name = "tasks_business_owner")
public class BusinessOwnerTask extends Task { @Override public DashboardProfile getProfile() { return DashboardProfile.BUSINESS_OWNER; } }

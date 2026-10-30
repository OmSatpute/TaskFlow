package com.taskflow.api;

public record DashboardSummary(long total, long pending, long inProgress, long completed,
                               long overdue, long dueToday, long dueSoon) { }

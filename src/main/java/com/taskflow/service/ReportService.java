package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.DashboardProfile;
import com.taskflow.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final TaskRepository repository;
    public ReportService(TaskRepository repository) { this.repository = repository; }

    public String csv(LocalDate from, LocalDate to, DashboardProfile profile, Long ownerId) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date.");
        }
        List<Task> tasks = repository.findByProfileOrderByDeadlineAsc(profile, ownerId).stream()
                .filter(t -> (from == null && to == null)
                        || (t.getDeadline() != null
                        && (from == null || !t.getDeadline().toLocalDate().isBefore(from))
                        && (to == null || !t.getDeadline().toLocalDate().isAfter(to))))
                .toList();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long pending = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long overdue = tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED
                && t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now())).count();
        StringBuilder out = new StringBuilder("TaskFlow task report\r\n");
        out.append("Deadline from,").append(from == null ? "Any" : from)
                .append(",Deadline to,").append(to == null ? "Any" : to).append("\r\n");
        out.append("Total tasks,").append(tasks.size()).append("\r\nCompleted,").append(completed)
                .append("\r\nIn progress,").append(inProgress).append("\r\nPending,").append(pending)
                .append("\r\nOverdue open tasks,").append(overdue).append("\r\n\r\n");
        out.append("Title,Description,Domain,Category,Priority,Status,Deadline,Created At\r\n");
        tasks.forEach(t -> out.append(cell(t.getTitle())).append(',')
                .append(cell(t.getDescription())).append(',')
                .append(cell(t.getCategory() == null ? "" : t.getCategory().getDomain())).append(',')
                .append(cell(t.getCategory() == null ? "" : t.getCategory().getName())).append(',')
                .append(cell(t.getPriority().name())).append(',').append(cell(t.getStatus().name())).append(',')
                .append(cell(t.getDeadline() == null ? "" : t.getDeadline().toString())).append(',')
                .append(cell(t.getCreatedAt() == null ? "" : t.getCreatedAt().toString())).append("\r\n"));
        return out.toString();
    }

    private String cell(String value) {
        if (value == null) return "\"\"";
        String safe = value.replace("\r", " ").replace("\n", " ");
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}

package com.taskflow.service;

import com.taskflow.api.DashboardSummary;
import com.taskflow.api.TaskRequest;
import com.taskflow.model.*;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TaskCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final TaskCategoryRepository categories;
    public TaskService(TaskRepository repository, TaskCategoryRepository categories) {
        this.repository = repository;
        this.categories = categories;
    }

    public List<Task> list(DashboardProfile profile, Long ownerId, TaskStatus status, TaskPriority priority) {
        if (profile == null) profile = DashboardProfile.PERSONAL;
        List<Task> tasks = repository.findByProfileOrderByDeadlineAsc(profile, ownerId).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority).toList();
        tasks = new ArrayList<>(tasks);
        LocalDateTime now = LocalDateTime.now();
        tasks.sort((a, b) -> compareTasks(a, b, now));
        return tasks;
    }

    private int compareTasks(Task a, Task b, LocalDateTime now) {
        LocalDateTime da = a.getDeadline(), db = b.getDeadline();
        if (da == null || db == null) return da == db ? 0 : da == null ? 1 : -1;
        int dayOrder = da.toLocalDate().compareTo(db.toLocalDate());
        if (dayOrder != 0) return dayOrder;
        boolean urgentA = a.getStatus() != TaskStatus.COMPLETED && !da.isAfter(now.plusHours(1));
        boolean urgentB = b.getStatus() != TaskStatus.COMPLETED && !db.isAfter(now.plusHours(1));
        if (urgentA != urgentB) return urgentA ? -1 : 1;
        if (urgentA) return da.compareTo(db);
        int priorityOrder = Integer.compare(priorityRank(b.getPriority()), priorityRank(a.getPriority()));
        return priorityOrder != 0 ? priorityOrder : da.compareTo(db);
    }

    private int priorityRank(TaskPriority priority) {
        return switch (priority) { case HIGH -> 3; case MEDIUM -> 2; case LOW -> 1; };
    }
    public Task get(DashboardProfile profile, Long ownerId, long id) {
        return repository.findByProfileAndOwnerIdAndId(profile, ownerId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
    public Task create(TaskRequest input, Long ownerId, DashboardProfile profile) {
        Task task = switch (profile) {
            case PERSONAL -> new PersonalTask(); case STUDENT -> new StudentTask();
            case OFFICE_WORKER -> new OfficeWorkerTask(); case FREELANCER -> new FreelancerTask();
            case BUSINESS_OWNER -> new BusinessOwnerTask(); case TEACHER -> new TeacherTask();
        };
        return repository.save(profile, apply(task, input, profile, ownerId));
    }
    public Task update(DashboardProfile profile, Long ownerId, long id, TaskRequest input) {
        if (input.profile() != null && input.profile() != profile) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task profile must match its profile table.");
        }
        return repository.save(profile, apply(get(profile, ownerId, id), input, profile, ownerId));
    }
    public void delete(DashboardProfile profile, Long ownerId, long id) { repository.delete(profile, get(profile, ownerId, id)); }

    public DashboardSummary summary(DashboardProfile profile, Long ownerId) {
        if (profile == null) profile = DashboardProfile.PERSONAL;
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = repository.findByProfileOrderByDeadlineAsc(profile, ownerId);
        long pending = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long overdue = tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null && t.getDeadline().isBefore(now)).count();
        LocalDateTime startTomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        LocalDateTime endWeek = startTomorrow.plusDays(7).minusNanos(1);
        long dueToday = tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null
                && !t.getDeadline().isBefore(now) && t.getDeadline().isBefore(startTomorrow)).count();
        long dueSoon = tasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null
                && !t.getDeadline().isBefore(startTomorrow) && !t.getDeadline().isAfter(endWeek)).count();
        return new DashboardSummary(tasks.size(), pending, inProgress, completed, overdue, dueToday, dueSoon);
    }
    private Task apply(Task task, TaskRequest input, DashboardProfile profile, Long ownerId) {
        task.setTitle(input.title().trim());
        task.setDescription(input.description() == null ? "" : input.description().trim());
        task.setPriority(input.priority());
        task.setStatus(input.status());
        task.setDeadline(input.deadline());
        task.setCategory(input.categoryId() == null ? null : categories.findById(input.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found")));
        task.setProfile(profile);
        task.setOwnerId(ownerId);
        return task;
    }

    public List<String> tips(DashboardProfile profile, Long ownerId) {
        if (profile == null) profile = DashboardProfile.PERSONAL;
        LocalDateTime now = LocalDateTime.now();
        List<Task> open = repository.findByProfileOrderByDeadlineAsc(profile, ownerId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED).toList();
        List<String> result = new ArrayList<>();
        long overdue = open.stream().filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(now)).count();
        long dueToday = open.stream().filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().equals(now.toLocalDate())).count();
        long high = open.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
        if (overdue > 0) result.add("Review " + overdue + " overdue task" + (overdue == 1 ? "" : "s") + " and update the deadlines or mark them complete.");
        if (dueToday > 0) result.add("You have " + dueToday + " open task" + (dueToday == 1 ? "" : "s") + " due today. Consider starting with the earliest deadline.");
        if (high >= 3) result.add("There are " + high + " high-priority tasks still open. Focus on one at a time and reassess the rest.");
        if (open.size() >= 8) result.add("Your open list has " + open.size() + " tasks. Grouping them by category can make the next step easier to see.");
        if (result.isEmpty()) result.add(open.isEmpty()
                ? "Your open list is clear. Add a task when something needs your attention."
                : "You’re up to date. Set a clear deadline on the next time-sensitive task.");
        return result.stream().limit(3).toList();
    }
}

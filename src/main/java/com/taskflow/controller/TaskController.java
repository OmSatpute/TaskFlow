package com.taskflow.controller;

import com.taskflow.api.DashboardSummary;
import com.taskflow.api.CategoryRequest;
import com.taskflow.api.TaskRequest;
import com.taskflow.api.TipResponse;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.DashboardProfile;
import com.taskflow.model.AppUser;
import com.taskflow.service.TaskService;
import com.taskflow.service.CategoryService;
import com.taskflow.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskService service;
    private final CategoryService categoryService;
    private final ReportService reportService;
    public TaskController(TaskService service, CategoryService categoryService, ReportService reportService) {
        this.service = service;
        this.categoryService = categoryService;
        this.reportService = reportService;
    }

    @GetMapping("/tasks")
    public List<Task> list(@RequestParam(required = false) TaskStatus status,
                           @RequestParam(required = false) TaskPriority priority,
                           @RequestAttribute("currentUser") AppUser user) {
        return service.list(user.getProfile(), user.getId(), status, priority);
    }
    @GetMapping("/tasks/{id}")
    public Task get(@PathVariable long id, @RequestAttribute("currentUser") AppUser user) {
        return service.get(user.getProfile(), user.getId(), id);
    }
    @PostMapping("/tasks") @ResponseStatus(HttpStatus.CREATED)
    public Task create(@Valid @RequestBody TaskRequest input, @RequestAttribute("currentUser") AppUser user) {
        return service.create(input, user.getId(), user.getProfile());
    }
    @PutMapping("/tasks/{id}")
    public Task update(@PathVariable long id, @Valid @RequestBody TaskRequest input,
                       @RequestAttribute("currentUser") AppUser user) {
        return service.update(user.getProfile(), user.getId(), id, input);
    }
    @DeleteMapping("/tasks/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, @RequestAttribute("currentUser") AppUser user) { service.delete(user.getProfile(), user.getId(), id); }
    @GetMapping("/dashboard") public DashboardSummary dashboard(@RequestAttribute("currentUser") AppUser user) { return service.summary(user.getProfile(), user.getId()); }
    @GetMapping("/tips") public List<TipResponse> tips(@RequestAttribute("currentUser") AppUser user) { return service.tips(user.getProfile(), user.getId()).stream().map(TipResponse::new).toList(); }
    @GetMapping("/categories") public List<TaskCategory> categories() { return categoryService.list(); }
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public TaskCategory createCategory(@Valid @RequestBody CategoryRequest request) { return categoryService.create(request); }
    @GetMapping(value = "/reports.csv", produces = "text/csv")
    public ResponseEntity<String> report(@RequestParam(required = false) LocalDate from,
                                         @RequestParam(required = false) LocalDate to,
                                         @RequestAttribute("currentUser") AppUser user) {
        return ResponseEntity.ok().contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=taskflow-report.csv")
                .body(reportService.csv(from, to, user.getProfile(), user.getId()));
    }
}

package com.taskflow.service;

import com.taskflow.api.CategoryRequest;
import com.taskflow.model.TaskCategory;
import com.taskflow.repository.TaskCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CategoryService {
    private final TaskCategoryRepository repository;
    public CategoryService(TaskCategoryRepository repository) { this.repository = repository; }

    public List<TaskCategory> list() { return repository.findAllByOrderByDomainAscNameAsc(); }

    public TaskCategory create(CategoryRequest request) {
        String domain = request.domain().trim();
        String name = request.name().trim();
        if (repository.existsByDomainIgnoreCaseAndNameIgnoreCase(domain, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That category already exists in this domain.");
        }
        return repository.save(new TaskCategory(domain, name));
    }
}

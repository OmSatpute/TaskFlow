package com.taskflow.repository;

import com.taskflow.model.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {
    List<TaskCategory> findAllByOrderByDomainAscNameAsc();
    boolean existsByDomainIgnoreCaseAndNameIgnoreCase(String domain, String name);
}

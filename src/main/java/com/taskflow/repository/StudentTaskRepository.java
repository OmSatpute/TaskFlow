package com.taskflow.repository;
import com.taskflow.model.StudentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StudentTaskRepository extends JpaRepository<StudentTask, Long> {
    List<StudentTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<StudentTask> findByOwnerIdAndId(Long ownerId, Long id);
}

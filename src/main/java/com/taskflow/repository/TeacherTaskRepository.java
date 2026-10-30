package com.taskflow.repository;
import com.taskflow.model.TeacherTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TeacherTaskRepository extends JpaRepository<TeacherTask, Long> {
    List<TeacherTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<TeacherTask> findByOwnerIdAndId(Long ownerId, Long id);
}

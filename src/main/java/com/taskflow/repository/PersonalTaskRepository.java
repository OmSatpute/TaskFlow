package com.taskflow.repository;
import com.taskflow.model.PersonalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PersonalTaskRepository extends JpaRepository<PersonalTask, Long> {
    List<PersonalTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<PersonalTask> findByOwnerIdAndId(Long ownerId, Long id);
}

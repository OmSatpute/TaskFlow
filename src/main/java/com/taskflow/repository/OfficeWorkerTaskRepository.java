package com.taskflow.repository;
import com.taskflow.model.OfficeWorkerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface OfficeWorkerTaskRepository extends JpaRepository<OfficeWorkerTask, Long> {
    List<OfficeWorkerTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<OfficeWorkerTask> findByOwnerIdAndId(Long ownerId, Long id);
}

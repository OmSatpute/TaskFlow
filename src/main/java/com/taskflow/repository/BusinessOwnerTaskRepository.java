package com.taskflow.repository;
import com.taskflow.model.BusinessOwnerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface BusinessOwnerTaskRepository extends JpaRepository<BusinessOwnerTask, Long> {
    List<BusinessOwnerTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<BusinessOwnerTask> findByOwnerIdAndId(Long ownerId, Long id);
}

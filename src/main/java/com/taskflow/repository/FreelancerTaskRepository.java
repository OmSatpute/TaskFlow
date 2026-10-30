package com.taskflow.repository;
import com.taskflow.model.FreelancerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FreelancerTaskRepository extends JpaRepository<FreelancerTask, Long> {
    List<FreelancerTask> findByOwnerIdOrderByDeadlineAsc(Long ownerId);
    Optional<FreelancerTask> findByOwnerIdAndId(Long ownerId, Long id);
}

package com.taskflow.repository;

import com.taskflow.model.*;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Routes shared task operations to the physical table for the chosen profile. */
@Repository
public class TaskRepository {
    private final PersonalTaskRepository personal;
    private final StudentTaskRepository student;
    private final OfficeWorkerTaskRepository officeWorker;
    private final FreelancerTaskRepository freelancer;
    private final BusinessOwnerTaskRepository businessOwner;
    private final TeacherTaskRepository teacher;

    public TaskRepository(PersonalTaskRepository personal, StudentTaskRepository student,
                         OfficeWorkerTaskRepository officeWorker, FreelancerTaskRepository freelancer,
                         BusinessOwnerTaskRepository businessOwner, TeacherTaskRepository teacher) {
        this.personal = personal; this.student = student; this.officeWorker = officeWorker;
        this.freelancer = freelancer; this.businessOwner = businessOwner; this.teacher = teacher;
    }

    public List<Task> findByProfileOrderByDeadlineAsc(DashboardProfile profile, Long ownerId) {
        List<Task> tasks = new ArrayList<>();
        switch (profile) {
            case PERSONAL -> tasks.addAll(personal.findByOwnerIdOrderByDeadlineAsc(ownerId));
            case STUDENT -> tasks.addAll(student.findByOwnerIdOrderByDeadlineAsc(ownerId));
            case OFFICE_WORKER -> tasks.addAll(officeWorker.findByOwnerIdOrderByDeadlineAsc(ownerId));
            case FREELANCER -> tasks.addAll(freelancer.findByOwnerIdOrderByDeadlineAsc(ownerId));
            case BUSINESS_OWNER -> tasks.addAll(businessOwner.findByOwnerIdOrderByDeadlineAsc(ownerId));
            case TEACHER -> tasks.addAll(teacher.findByOwnerIdOrderByDeadlineAsc(ownerId));
        }
        return tasks;
    }

    public Optional<Task> findByProfileAndOwnerIdAndId(DashboardProfile profile, Long ownerId, long id) {
        return switch (profile) {
            case PERSONAL -> personal.findByOwnerIdAndId(ownerId,id).map(t -> t);
            case STUDENT -> student.findByOwnerIdAndId(ownerId,id).map(t -> t);
            case OFFICE_WORKER -> officeWorker.findByOwnerIdAndId(ownerId,id).map(t -> t);
            case FREELANCER -> freelancer.findByOwnerIdAndId(ownerId,id).map(t -> t);
            case BUSINESS_OWNER -> businessOwner.findByOwnerIdAndId(ownerId,id).map(t -> t);
            case TEACHER -> teacher.findByOwnerIdAndId(ownerId,id).map(t -> t);
        };
    }

    public Task save(DashboardProfile profile, Task task) {
        return switch (profile) {
            case PERSONAL -> personal.save(as(task, PersonalTask.class));
            case STUDENT -> student.save(as(task, StudentTask.class));
            case OFFICE_WORKER -> officeWorker.save(as(task, OfficeWorkerTask.class));
            case FREELANCER -> freelancer.save(as(task, FreelancerTask.class));
            case BUSINESS_OWNER -> businessOwner.save(as(task, BusinessOwnerTask.class));
            case TEACHER -> teacher.save(as(task, TeacherTask.class));
        };
    }

    public void delete(DashboardProfile profile, Task task) {
        switch (profile) {
            case PERSONAL -> personal.delete(as(task, PersonalTask.class));
            case STUDENT -> student.delete(as(task, StudentTask.class));
            case OFFICE_WORKER -> officeWorker.delete(as(task, OfficeWorkerTask.class));
            case FREELANCER -> freelancer.delete(as(task, FreelancerTask.class));
            case BUSINESS_OWNER -> businessOwner.delete(as(task, BusinessOwnerTask.class));
            case TEACHER -> teacher.delete(as(task, TeacherTask.class));
        }
    }

    private <T extends Task> T as(Task task, Class<T> type) {
        if (!type.isInstance(task)) throw new IllegalArgumentException("Task belongs to a different profile table.");
        return type.cast(task);
    }
}

package com.taskflow.service;

import com.taskflow.api.LoginRequest;
import com.taskflow.api.RegisterRequest;
import com.taskflow.model.AppUser;
import com.taskflow.model.DashboardProfile;
import com.taskflow.repository.AppUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppUserService {
    private final AppUserRepository users;
    private final PasswordHasher passwords;
    private final JdbcTemplate jdbc;
    public AppUserService(AppUserRepository users, PasswordHasher passwords, JdbcTemplate jdbc) {
        this.users=users; this.passwords=passwords; this.jdbc=jdbc;
    }

    public AppUser register(RegisterRequest input) {
        String email = input.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        AppUser user = users.save(new AppUser(input.name().trim(), email, passwords.hash(input.password()), input.profile()));
        String table = tableFor(input.profile());
        jdbc.update("UPDATE " + table + " SET owner_id = ? WHERE owner_id IS NULL", user.getId());
        return user;
    }

    public AppUser authenticate(LoginRequest input) {
        AppUser user = users.findByEmailIgnoreCase(input.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (!passwords.matches(input.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        }
        return user;
    }

    public AppUser get(long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    public static String tableFor(DashboardProfile profile) {
        return switch (profile) {
            case PERSONAL -> "tasks_personal"; case STUDENT -> "tasks_student";
            case OFFICE_WORKER -> "tasks_office_worker"; case FREELANCER -> "tasks_freelancer";
            case BUSINESS_OWNER -> "tasks_business_owner"; case TEACHER -> "tasks_teacher";
        };
    }
}

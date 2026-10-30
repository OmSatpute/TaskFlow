package com.taskflow.api;

import com.taskflow.model.AppUser;
import com.taskflow.model.DashboardProfile;

public record UserResponse(Long id, String name, String email, DashboardProfile profile) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getProfile());
    }
}

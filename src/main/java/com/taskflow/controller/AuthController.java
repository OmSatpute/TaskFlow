package com.taskflow.controller;

import com.taskflow.api.LoginRequest;
import com.taskflow.api.RegisterRequest;
import com.taskflow.api.UserResponse;
import com.taskflow.model.AppUser;
import com.taskflow.service.AppUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserService users;
    public AuthController(AppUserService users) { this.users=users; }

    @GetMapping("/csrf")
    public Map<String,String> csrf(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(ApiAuthenticationFilter.CSRF_SESSION_KEY);
        if (token == null) { token = UUID.randomUUID().toString(); session.setAttribute(ApiAuthenticationFilter.CSRF_SESSION_KEY, token); }
        return Map.of("token", token);
    }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest input, HttpServletRequest request) {
        return establish(users.register(input), request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest input, HttpServletRequest request) {
        return establish(users.authenticate(input), request);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("taskflowUserId") == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(UserResponse.from(users.get((Long) session.getAttribute("taskflowUserId"))));
    }

    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false); if (session != null) session.invalidate();
    }

    private UserResponse establish(AppUser user, HttpServletRequest request) {
        HttpSession old = request.getSession(false); if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("taskflowUserId", user.getId());
        session.setAttribute(ApiAuthenticationFilter.CSRF_SESSION_KEY, UUID.randomUUID().toString());
        return UserResponse.from(user);
    }
}

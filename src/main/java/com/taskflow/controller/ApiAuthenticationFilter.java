package com.taskflow.controller;

import com.taskflow.model.AppUser;
import com.taskflow.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(10)
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String CSRF_SESSION_KEY = "taskflowCsrfToken";
    private final AppUserRepository users;
    public ApiAuthenticationFilter(AppUserRepository users) { this.users=users; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean authEndpoint = path.equals("/api/auth/csrf") || path.equals("/api/auth/register")
                || path.equals("/api/auth/login") || path.equals("/api/auth/me") || path.equals("/api/auth/logout");
        if (requiresCsrf(request.getMethod()) && !validCsrf(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Refresh the page and try again.\"}"); return;
        }
        if (!authEndpoint) {
            HttpSession session = request.getSession(false);
            Object id = session == null ? null : session.getAttribute("taskflowUserId");
            if (!(id instanceof Long userId)) { unauthorized(response); return; }
            AppUser user = users.findById(userId).orElse(null);
            if (user == null) { unauthorized(response); return; }
            request.setAttribute("currentUser", user);
        }
        chain.doFilter(request,response);
    }

    private boolean requiresCsrf(String method) { return !(method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS")); }
    private boolean validCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object stored = session.getAttribute(CSRF_SESSION_KEY);
        String supplied = request.getHeader("X-CSRF-TOKEN");
        return stored instanceof String token && supplied != null
                && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Please sign in to continue.\"}");
    }
}

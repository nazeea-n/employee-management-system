package com.vei.ems.web;

import com.vei.ems.dto.AuditEntryResponse;
import com.vei.ems.dto.CreateUserRequest;
import com.vei.ems.dto.UserResponse;
import com.vei.ems.service.AuditService;
import com.vei.ems.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-only endpoints: user management and the activity log. */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final AuditService auditService;

    public AdminController(AuthService authService, AuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return authService.listUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        UserResponse created = authService.createUser(request);
        auditService.log(authentication.getName(), "CREATE_USER", created.username() + " role=" + created.role());
        return created;
    }

    @GetMapping("/audit")
    public List<AuditEntryResponse> audit() {
        return auditService.latest();
    }
}

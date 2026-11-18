package com.vei.ems.service;

import com.vei.ems.domain.AppUser;
import com.vei.ems.domain.Role;
import com.vei.ems.dto.CreateUserRequest;
import com.vei.ems.dto.LoginResponse;
import com.vei.ems.dto.UserResponse;
import com.vei.ems.exception.BadRequestException;
import com.vei.ems.exception.ConflictException;
import com.vei.ems.exception.UnauthorizedException;
import com.vei.ems.repository.AppUserRepository;
import com.vei.ems.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Login, user creation and password changes. */
@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        AppUser user = users.findByUsername(username.trim().toLowerCase())
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        return new LoginResponse(jwtService.generateToken(user.getUsername(), user.getRole()),
                user.getUsername(), user.getRole().name());
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (!encoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        PasswordPolicy.validate(newPassword);
        user.setPasswordHash(encoder.encode(newPassword));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase();
        PasswordPolicy.validate(request.password());
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
        AppUser saved = users.save(new AppUser(username, encoder.encode(request.password()), request.role()));
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole().name()))
                .collect(Collectors.toList());
    }

    /** Used on first start-up so there is always someone who can log in. */
    @Transactional
    public void createDefaultAdminIfNoUsers(String password) {
        if (users.count() == 0) {
            users.save(new AppUser("admin", encoder.encode(password), Role.ADMIN));
        }
    }
}

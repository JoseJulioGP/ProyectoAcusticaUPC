package com.upc.acusticupc.auth.application.service;

import com.upc.acusticupc.auth.application.dto.*;
import com.upc.acusticupc.auth.domain.model.Role;
import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserListItemDTO> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public UserListItemDTO get(UUID id) {
        return toListItem(findOr404(id));
    }

    @Transactional
    public UserListItemDTO create(CreateUserRequest req) {
        String email = req.email().toLowerCase().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User u = User.builder()
                .fullName(req.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .active(true)
                .build();
        u = userRepository.save(u);
        log.info("Usuario creado: {} (rol {})", u.getEmail(), u.getRole());
        return toListItem(u);
    }

    @Transactional
    public UserListItemDTO update(UUID id, UpdateUserRequest req) {
        User u = findOr404(id);
        // Si este usuario es ADMIN activo y lo van a degradar o desactivar, proteger al último.
        boolean losingAdmin = u.getRole() == Role.ADMIN
                && u.isActive()
                && (req.role() != Role.ADMIN || !req.active());
        if (losingAdmin && isLastActiveAdmin(u)) {
            throw new LastAdminException();
        }
        u.setFullName(req.fullName().trim());
        u.setRole(req.role());
        u.setActive(req.active());
        log.info("Usuario actualizado: {} (rol {}, activo {})", u.getEmail(), u.getRole(), u.isActive());
        return toListItem(userRepository.save(u));
    }

    @Transactional
    public void resetPassword(UUID id, ChangePasswordRequest req) {
        User u = findOr404(id);
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(u);
        log.info("Password reseteado para usuario {}", u.getEmail());
    }

    @Transactional
    public void deactivate(UUID id) {
        User u = findOr404(id);
        if (u.getRole() == Role.ADMIN && u.isActive() && isLastActiveAdmin(u)) {
            throw new LastAdminException();
        }
        u.setActive(false);                 // soft-delete
        userRepository.save(u);
        log.info("Usuario desactivado: {}", u.getEmail());
    }

    // ---- helpers ----

    private boolean isLastActiveAdmin(User candidate) {
        return userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1;
    }

    private User findOr404(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserListItemDTO toListItem(User u) {
        return new UserListItemDTO(
                u.getId(), u.getFullName(), u.getEmail(),
                u.getRole(), u.isActive(), u.getCreatedAt());
    }

    // ---- excepciones de dominio ----

    public static class EmailAlreadyUsedException extends RuntimeException {
        public EmailAlreadyUsedException(String email) {
            super("El email ya está registrado: " + email);
        }
    }
    public static class LastAdminException extends RuntimeException {
        public LastAdminException() {
            super("No se puede desactivar ni degradar al último administrador activo.");
        }
    }
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(UUID id) {
            super("Usuario no encontrado: " + id);
        }
    }
}

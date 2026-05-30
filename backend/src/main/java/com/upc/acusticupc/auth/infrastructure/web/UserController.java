package com.upc.acusticupc.auth.infrastructure.web;

import com.upc.acusticupc.auth.application.dto.*;
import com.upc.acusticupc.auth.application.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // todo el controller es solo-ADMIN
public class UserController {

    private final UserManagementService service;

    @GetMapping
    public Page<UserListItemDTO> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public UserListItemDTO get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<UserListItemDTO> create(@Valid @RequestBody CreateUserRequest req) {
        UserListItemDTO created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public UserListItemDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest req) {
        service.resetPassword(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        service.deactivate(id);
    }
}
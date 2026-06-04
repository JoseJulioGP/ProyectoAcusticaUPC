package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.service.FolderService;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderRequest;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<FolderResponse> list() {
        return folderService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public FolderResponse get(@PathVariable UUID id) {
        return folderService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody FolderRequest request,
                                                 @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(folderService.create(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public FolderResponse update(@PathVariable UUID id, @Valid @RequestBody FolderRequest request) {
        return folderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        folderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

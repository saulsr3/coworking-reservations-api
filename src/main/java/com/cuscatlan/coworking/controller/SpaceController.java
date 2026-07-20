package com.cuscatlan.coworking.controller;

import com.cuscatlan.coworking.dto.request.SpaceRequest;
import com.cuscatlan.coworking.dto.response.SpaceResponse;
import com.cuscatlan.coworking.entity.SpaceType;
import com.cuscatlan.coworking.service.SpaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@Tag(name = "Espacios", description = "Gestión de espacios de coworking")
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<SpaceResponse>> findAll(
            @RequestParam(required = false) SpaceType type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(spaceService.findAll(type, minCapacity, location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SpaceResponse> create(@Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceResponse> update(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        spaceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package com.cuscatlan.coworking.controller;

import com.cuscatlan.coworking.dto.request.ReservationRequest;
import com.cuscatlan.coworking.dto.response.ReservationResponse;
import com.cuscatlan.coworking.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Creación, consulta y cancelación de reservas")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                      Authentication authentication) {
        ReservationResponse response = reservationService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> findMine(Authentication authentication) {
        return ResponseEntity.ok(reservationService.findMine(authentication.getName()));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(reservationService.cancel(id, authentication.getName(), isAdmin));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}

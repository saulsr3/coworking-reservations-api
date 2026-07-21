package com.cuscatlan.coworking.controller;

import com.cuscatlan.coworking.dto.response.OccupancyResponse;
import com.cuscatlan.coworking.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes administrativos (solo ADMIN)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/occupancy")
    public ResponseEntity<List<OccupancyResponse>> occupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getOccupancyReport(from, to));
    }
}
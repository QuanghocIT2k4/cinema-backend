package com.cinema.controller;

import com.cinema.model.dto.request.RefreshmentRequest;
import com.cinema.model.dto.response.RefreshmentResponse;
import com.cinema.service.RefreshmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý Refreshment APIs
 */
@RestController
@RequestMapping("/api/refreshments")
@RequiredArgsConstructor
public class RefreshmentController {

    private final RefreshmentService refreshmentService;

    /**
     * GET /api/refreshments
     * Lấy danh sách đồ ăn/đồ uống đang bán (public)
     */
    @GetMapping
    public ResponseEntity<List<RefreshmentResponse>> getCurrentRefreshments() {
        List<RefreshmentResponse> refreshments = refreshmentService.getCurrentRefreshments();
        return ResponseEntity.ok(refreshments);
    }

    /**
     * POST /api/refreshments
     * Tạo refreshment mới (Admin only)
     */
    @PostMapping
    public ResponseEntity<RefreshmentResponse> createRefreshment(
            @Valid @RequestBody RefreshmentRequest request) {
        RefreshmentResponse response = refreshmentService.createRefreshment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}









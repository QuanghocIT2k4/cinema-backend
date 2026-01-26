package com.cinema.service;

import com.cinema.model.dto.request.RefreshmentRequest;
import com.cinema.model.dto.response.RefreshmentResponse;
import com.cinema.model.entity.Refreshment;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.RefreshmentRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý logic Refreshment (đồ ăn/đồ uống)
 */
@Service
@RequiredArgsConstructor
public class RefreshmentService {

    private final RefreshmentRepository refreshmentRepository;

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Không xác định được user hiện tại");
        }
        return userDetails;
    }

    private void checkAdminRole() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser.getUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Chỉ Admin mới có quyền thực hiện thao tác này");
        }
    }

    /**
     * GET /api/refreshments
     * Lấy danh sách đồ ăn/đồ uống đang bán
     */
    public List<RefreshmentResponse> getCurrentRefreshments() {
        return refreshmentRepository.findByIsCurrentTrue().stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * POST /api/refreshments
     * Tạo refreshment mới (Admin only)
     */
    @Transactional
    public RefreshmentResponse createRefreshment(RefreshmentRequest request) {
        checkAdminRole();

        Refreshment refreshment = new Refreshment();
        refreshment.setName(request.getName());
        refreshment.setPicture(request.getPicture());
        refreshment.setPrice(request.getPrice());
        refreshment.setIsCurrent(
                request.getIsCurrent() != null ? request.getIsCurrent() : Boolean.TRUE);

        Refreshment saved = refreshmentRepository.save(refreshment);
        return convertToResponse(saved);
    }

    private RefreshmentResponse convertToResponse(Refreshment refreshment) {
        RefreshmentResponse res = new RefreshmentResponse();
        res.setId(refreshment.getId());
        res.setName(refreshment.getName());
        res.setPicture(refreshment.getPicture());
        res.setPrice(refreshment.getPrice());
        res.setIsCurrent(refreshment.getIsCurrent());
        res.setCreatedAt(refreshment.getCreatedAt());
        res.setUpdatedAt(refreshment.getUpdatedAt());
        return res;
    }
}










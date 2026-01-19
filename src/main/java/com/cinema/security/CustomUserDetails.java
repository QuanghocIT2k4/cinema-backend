package com.cinema.security;

import com.cinema.model.entity.User;
import com.cinema.model.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * CustomUserDetails
 *
 * Bọc entity User của mình để Spring Security hiểu được:
 * - username (dùng để login).
 * - password (hash đã mã hoá).
 * - authorities (ROLE_ADMIN, ROLE_CUSTOMER...).
 * - trạng thái tài khoản (active, locked...).
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Trả về danh sách quyền (authorities) của user.
     * - Convention của Spring Security: ROLE_XXX
     * - Ở đây map từ enum UserRole -> SimpleGrantedAuthority("ROLE_" + roleName)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Các method bên dưới dùng để khoá/mở tài khoản

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

    // Một số helper để lấy thêm thông tin nếu cần

    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }
}



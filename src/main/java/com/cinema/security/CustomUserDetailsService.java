package com.cinema.security;

import com.cinema.model.entity.User;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService
 *
 * BƯỚC 3 theo roadmap: Code UserDetailsService.
 *
 * Nhiệm vụ:
 * - Nhận username từ Spring Security.
 * - Tìm User tương ứng trong database (UserRepository).
 * - Bọc User thành CustomUserDetails để Security dùng trong quá trình authenticate/authorize.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Dự án FE đang cho user đăng nhập bằng email (gmail),
        // nên ở đây ta coi "username" chính là email đăng nhập.
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new CustomUserDetails(user);
    }
}



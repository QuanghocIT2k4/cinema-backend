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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new CustomUserDetails(user);
    }
}



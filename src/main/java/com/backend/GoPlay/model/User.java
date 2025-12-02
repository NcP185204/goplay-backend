package com.backend.GoPlay.model;




import com.backend.GoPlay.util.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // Đổi tên bảng thành "users" cho chuẩn
public class User implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Đây sẽ là mật khẩu đã được băm

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING) // Lưu vai trò dưới dạng String (PLAYER, ADMIN)
    @Column(nullable = false)
    private UserRole role;

    // --- Các phương thức của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // --- LOGGING ĐỂ DEBUG ---
        if (role == null) {
            logger.error("QUAN TRỌNG: User role bị NULL khi tạo quyền cho user: {}", this.email);
            return List.of(); // Trả về danh sách rỗng nếu role là null
        }
        String authorityString = "ROLE_" + role.name();
        logger.info("Tạo quyền cho user: '{}', role: '{}', authority: '{}'", this.email, this.role, authorityString);
        // --- KẾT THÚC LOGGING ---

        return List.of(new SimpleGrantedAuthority(authorityString));
    }

    @Override
    public String getUsername() {
        // Chúng ta dùng email làm username
        return email;
    }

    // Các phương thức khác để đơn giản ta luôn trả về true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
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
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // --- MỐI QUAN HỆ NHIỀU-NHIỀU VỚI SÂN YÊU THÍCH ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_favorite_courts", // Tên bảng trung gian sẽ được tạo
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "court_id")
    )
    @Builder.Default // Đảm bảo trường này được khởi tạo khi dùng Builder
    private Set<Court> favoriteCourts = new HashSet<>();

    // --- Các phương thức của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            logger.error("QUAN TRỌNG: User role bị NULL khi tạo quyền cho user: {}", this.email);
            return List.of();
        }
        String authorityString = "ROLE_" + role.name();
        logger.info("Tạo quyền cho user: '{}', role: '{}', authority: '{}'", this.email, this.role, authorityString);
        return List.of(new SimpleGrantedAuthority(authorityString));
    }

    @Override
    public String getUsername() {
        return email;
    }

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

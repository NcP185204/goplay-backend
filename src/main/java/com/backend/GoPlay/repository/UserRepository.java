package com.backend.GoPlay.repository;
import com.backend.GoPlay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Hàm Spring Data JPA tự hiểu: "Tìm một User theo cột email"
    Optional<User> findByEmail(String email);

    // Hàm Spring Data JPA tự hiểu: "Kiểm tra xem User với email này có tồn tại không"
    Boolean existsByEmail(String email);
}

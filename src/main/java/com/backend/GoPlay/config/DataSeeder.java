package com.backend.GoPlay.config;

import com.backend.GoPlay.util.FacilityService;
import com.backend.GoPlay.util.SportType;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.util.UserRole;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Mật khẩu test chung cho các tài khoản mẫu
    private final String TEST_PASSWORD = "password";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- KÍCH HOẠT DATA SEEDER ---");

        // 1. TẠO HOẶC LẤY USER CHỦ SÂN (MANAGER) VÀ NGƯỜI CHƠI (PLAYER)
        User manager = createOrGetTestUser("manager@goplay.com", "Quản Lý Test", UserRole.MANAGER);
        createOrGetTestUser("player@goplay.com", "Người Chơi Test", UserRole.PLAYER);

        // 2. NẠP DỮ LIỆU SÂN TỪ CSV (Chỉ chạy nếu bảng Court trống)
        if (courtRepository.count() == 0) {
            loadCourtsFromCsv(manager);
        }

        System.out.println("--- DATA SEEDER HOÀN TẤT ---");
    }

    /**
     * Hàm helper để tạo hoặc lấy User đã tồn tại
     */
    private User createOrGetTestUser(String email, String fullName, UserRole role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .fullName(fullName)
                            .email(email)
                            .password(passwordEncoder.encode(TEST_PASSWORD))
                            .role(role)
                            .build();
                    System.out.println("✅ User " + role.name() + " (" + email + ") đã được tạo.");
                    return userRepository.save(user);
                });
    }

    /**
     * Hàm đọc file courts.csv và nạp vào database
     */
    private void loadCourtsFromCsv(User manager) {
        List<Court> courts = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("courts.csv");

        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {

            reader.readNext(); // Bỏ qua dòng Header (tiêu đề cột)
            String[] line;
            int count = 0;

            while ((line = reader.readNext()) != null) {
                // Kiểm tra đủ 7 cột cần thiết
                if (line.length < 7) continue;

                try {
                    Court court = Court.builder()
                            .name(line[0])
                            .address(line[1])
                            // Chuyển String sang Enum (ví dụ: "FUTSAL")
                            .courtType(SportType.valueOf(line[2].toUpperCase()))
                            .pricePerHour(Double.parseDouble(line[3]))
                            .latitude(Double.parseDouble(line[4]))
                            .longitude(Double.parseDouble(line[5]))
                            .imageUrls(List.of(line[6]))
                            .description("Sân được nạp tự động từ CSV.")
                            .owner(manager)
                            .averageRating(4.0 + Math.random()) // Random rating 4.0 - 5.0
                            .services(Set.of(FacilityService.PARKING, FacilityService.WC))
                            .build();

                    courts.add(court);
                    count++;
                } catch (Exception e) {
                    System.err.println("❌ Lỗi dữ liệu CSV (Dòng bị bỏ qua): " + Arrays.toString(line) + ". Chi tiết: " + e.getMessage());
                }
            }

            // Lưu hàng loạt (Batch Insert)
            courtRepository.saveAll(courts);
            System.out.println("--- Đã nạp thành công " + count + " sân từ CSV! ---");

        } catch (Exception e) {
            System.err.println("❌ LỖI KHÔNG ĐỌC ĐƯỢC FILE CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
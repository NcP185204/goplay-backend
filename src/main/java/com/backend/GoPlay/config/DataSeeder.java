package com.backend.GoPlay.config;

import com.backend.GoPlay.model.CourtImage;
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

    private final String TEST_PASSWORD = "password";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- KÍCH HOẠT DATA SEEDER ---");

        User manager = createOrGetTestUser("manager@goplay.com", "Quản Lý Test", UserRole.MANAGER);
        createOrGetTestUser("player@goplay.com", "Người Chơi Test", UserRole.PLAYER);

        if (courtRepository.count() == 0) {
            loadCourtsFromCsv(manager);
        }

        System.out.println("--- DATA SEEDER HOÀN TẤT ---");
    }

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

    private void loadCourtsFromCsv(User manager) {
        List<Court> courts = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("courts.csv");

        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {

            reader.readNext(); // Bỏ qua dòng Header
            String[] line;
            int count = 0;

            while ((line = reader.readNext()) != null) {
                // Yêu cầu file CSV phải có ít nhất 8 cột
                if (line.length < 8) continue;

                try {
                    // 1. Tạo đối tượng Court với các thông tin cơ bản và thumbnail
                    Court court = Court.builder()
                            .name(line[0])
                            .address(line[1])
                            .courtType(SportType.valueOf(line[2].toUpperCase()))
                            .pricePerHour(Double.parseDouble(line[3]))
                            .latitude(Double.parseDouble(line[4]))
                            .longitude(Double.parseDouble(line[5]))
                            .thumbnailUrl(line[6]) // Gán ảnh đại diện từ cột 6
                            .description("Sân được nạp tự động từ CSV.")
                            .owner(manager)
                            .averageRating(4.0 + Math.random())
                            .services(Set.of(FacilityService.PARKING, FacilityService.WC))
                            .build();

                    // 2. Xử lý danh sách ảnh chi tiết từ cột 7
                    String detailImagesString = line[7];
                    if (detailImagesString != null && !detailImagesString.trim().isEmpty()) {
                        String[] imageUrls = detailImagesString.split(";");

                        for (String imageUrl : imageUrls) {
                            if (imageUrl.trim().isEmpty()) continue;

                            CourtImage newImage = CourtImage.builder()
                                    .imageUrl(imageUrl.trim())
                                    .court(court) // Thiết lập mối quan hệ 2 chiều
                                    .build();

                            court.getImages().add(newImage);
                        }
                    }

                    courts.add(court);
                    count++;
                } catch (Exception e) {
                    System.err.println("❌ Lỗi dữ liệu CSV (Dòng bị bỏ qua): " + Arrays.toString(line) + ". Chi tiết: " + e.getMessage());
                }
            }

            // Lưu hàng loạt. Nhờ CascadeType.ALL, cả Court và CourtImage sẽ được lưu.
            courtRepository.saveAll(courts);
            System.out.println("--- Đã nạp thành công " + count + " sân từ CSV! ---");

        } catch (Exception e) {
            System.err.println("❌ LỖI KHÔNG ĐỌC ĐƯỢC FILE CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

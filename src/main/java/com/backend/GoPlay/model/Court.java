package com.backend.GoPlay.model;

import com.backend.GoPlay.util.FacilityService; // Import Enum cho các tiện ích của sân
import com.backend.GoPlay.util.SportType;      // Import Enum cho loại hình thể thao
import jakarta.persistence.*;               // Import các annotation của JPA (Java Persistence API)
import lombok.AllArgsConstructor;             // Import Lombok để sinh Constructor đầy đủ tham số
import lombok.Builder;                        // Import Lombok để hỗ trợ Builder Pattern
import lombok.Data;                           // Import Lombok để sinh Getter, Setter, toString, etc.
import lombok.NoArgsConstructor;              // Import Lombok để sinh Constructor rỗng

import java.util.List;
import java.util.Set;

/**
 * Class Court: Đại diện cho thực thể Sân thể thao, ánh xạ tới bảng "courts" trong Database.
 */
@Data // Tự động sinh Getter, Setter, toString, equals, hashCode
@Builder // Hỗ trợ khởi tạo đối tượng bằng Builder Pattern
@NoArgsConstructor // Sinh Constructor không tham số (cần thiết cho JPA)
@AllArgsConstructor // Sinh Constructor với tất cả tham số
@Entity // Đánh dấu đây là một Entity JPA
@Table(name = "courts", indexes = {
        // Thiết lập các Index để tối ưu hiệu suất tìm kiếm và lọc dữ liệu
        @Index(name = "idx_court_name", columnList = "name"),
        @Index(name = "idx_court_type", columnList = "courtType"),
        @Index(name = "idx_price", columnList = "pricePerHour")
})
public class Court {

    // --- KHÓA CHÍNH (PRIMARY KEY) ---
    @Id // Đánh dấu là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Cho phép DB tự động tăng giá trị (Auto Increment)
    private Integer id;

    // --- MỐI QUAN HỆ (RELATIONSHIP) ---
    @ManyToOne(fetch = FetchType.LAZY)
    // Quan hệ N-1: Nhiều sân thuộc về Một chủ sở hữu.
    // LAZY: Chỉ tải thông tin User khi gọi phương thức getOwner(), tối ưu hiệu năng.
    @JoinColumn(name = "owner_id", nullable = false)
    // Định nghĩa cột khóa ngoại là 'owner_id' và không được phép NULL.
    private User owner;

    // --- THÔNG TIN CƠ BẢN ---
    @Column(nullable = false) private String name;    // Tên sân (Không được NULL)
    @Column(nullable = false) private String address; // Địa chỉ (Không được NULL)
    @Column(columnDefinition = "TEXT") private String description; // Mô tả, sử dụng kiểu TEXT cho nội dung dài

    // --- LOẠI HÌNH THỂ THAO (ENUM) ---
    @Enumerated(EnumType.STRING)
    // Lưu giá trị Enum (SportType) dưới dạng chuỗi ký tự (vd: "SOCCER")
    @Column(nullable = false) private SportType courtType; // Loại sân (bóng đá, cầu lông,...)

    // --- THÔNG TIN ĐỊNH GIÁ & ĐỊNH VỊ ---
    private Double pricePerHour;
    private Double latitude;
    private Double longitude;
    private Double averageRating = 0.0; // Điểm đánh giá trung bình, mặc định là 0.0

    // --- TẬP HỢP GIÁ TRỊ ĐƠN GIẢN (BẢNG PHỤ) ---

    // 1. Danh sách ảnh sân
    @ElementCollection(fetch = FetchType.EAGER)
    // Tạo bảng phụ để lưu List<String>. EAGER: Tải ảnh ngay lập tức cùng với Court.
    @CollectionTable(name = "court_images", joinColumns = @JoinColumn(name = "court_id"))
    // Tên bảng phụ là 'court_images', liên kết với bảng 'courts' qua cột 'court_id'.
    @Column(name = "image_url")
    private List<String> imageUrls;

    // 2. Tập hợp dịch vụ/tiện ích
    @ElementCollection(fetch = FetchType.EAGER)
    // Tạo bảng phụ cho tập hợp Enum FacilityService. EAGER: Tải dịch vụ ngay lập tức.
    @CollectionTable(name = "court_services", joinColumns = @JoinColumn(name = "court_id"))
    // Tên bảng phụ là 'court_services'.
    @Enumerated(EnumType.STRING)
    @Column(name = "service")
    private Set<FacilityService> services; // Dùng Set để đảm bảo mỗi dịch vụ chỉ xuất hiện 1 lần.
}
└── backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/goplay/
│   │   │       │
│   │   │       ├── GoPlayBackendApplication.java       // File chạy chính, chứa hàm main() để khởi động Spring Boot
│   │   │       │
│   │   │       ├── config/                             // --- LỚP CẤU HÌNH ---
│   │   │       │   ├── SecurityConfig.java             // Cấu hình Spring Security (PasswordEncoder, Filter Chain, JWT)
│   │   │       │   ├── OpenApiConfig.java              // Cấu hình tài liệu API (Swagger), thông tin chung, bảo mật
│   │   │       │   └── CloudinaryConfig.java           // Cấu hình API key, secret để kết nối dịch vụ upload ảnh
│   │   │       │
│   │   │       ├── controller/                         // --- LỚP CONTROLLER (API) --- (Nhận request và trả response)
│   │   │       │   ├── AuthController.java             // Nhận request /login, /register
│   │   │       │   ├── UserController.java             // Nhận request /users/me (lấy profile)
│   │   │       │   ├── CourtController.java            // Nhận request /courts (tìm sân, xem chi tiết sân)
│   │   │       │   ├── BookingController.java          // Nhận request /bookings (tạo đơn, xem lịch sử)
│   │   │       │   ├── PaymentController.java          // Nhận callback (webhook) từ cổng thanh toán (MoMo/VNPay)
│   │   │       │   ├── NotificationController.java     // Nhận request /notifications (lấy danh sách, đánh dấu đã đọc)
│   │   │       │   └── AdminController.java            // Nhận request /admin/... (cho các tính năng của admin)
│   │   │       │
│   │   │       ├── service/                            // --- LỚP SERVICE (LOGIC NGHIỆP VỤ) ---
│   │   │       │   ├── AuthService.java                // Interface: Định nghĩa các hàm (register, login)
│   │   │       │   ├── UserService.java                // Interface: Định nghĩa hàm (getProfile, updateProfile)
│   │   │       │   ├── CourtService.java               // Interface: Định nghĩa hàm (search, findById, create, update)
│   │   │       │   ├── BookingService.java             // Interface: Định nghĩa hàm (createBooking, cancelBooking)
│   │   │       │   ├── PaymentService.java             // Interface: Định nghĩa hàm (createPaymentUrl, handleWebhook)
│   │   │       │   ├── NotificationService.java        // Interface: Định nghĩa hàm (sendNotification, getMyNotifications)
│   │   │       │   └── AdminService.java               // Interface: Định nghĩa hàm (getDashboardStats, approveCourt)
│   │   │       │   │
│   │   │       │   └── impl/                           // Thư mục con chứa các file triển khai (implementation)
│   │   │       │       ├── AuthServiceImpl.java        // Code logic thật của AuthService
│   │   │       │       ├── UserServiceImpl.java        // Code logic thật của UserService
│   │   │       │       ├── CourtServiceImpl.java       // Code logic thật của CourtService
│   │   │       │       ├── BookingServiceImpl.java     // Code logic thật của BookingService
│   │   │       │       ├── PaymentServiceImpl.java     // Code logic thật của PaymentService
│   │   │       │       ├── NotificationServiceImpl.java// Code logic thật của NotificationService
│   │   │       │       └── AdminServiceImpl.java       // Code logic thật của AdminService
│   │   │       │
│   │   │       ├── repository/                         // --- LỚP REPOSITORY (TRUY CẬP CSDL) --- (Interface JpaRepository)
│   │   │       │   ├── UserRepository.java           // Cung cấp hàm truy vấn bảng 'users' (vd: findByEmail)
│   │   │       │   ├── CourtRepository.java            // Cung cấp hàm truy vấn bảng 'courts'
│   │   │       │   ├── BookingRepository.java          // Cung cấp hàm truy vấn bảng 'bookings'
│   │   │       │   ├── TimeSlotRepository.java         // Cung cấp hàm truy vấn bảng 'time_slots'
│   │   │       │   ├── PaymentRepository.java          // Cung cấp hàm truy vấn bảng 'payments'
│   │   │       │   └── NotificationRepository.java     // Cung cấp hàm truy vấn bảng 'notifications'
│   │   │       │
│   │   │       ├── model/ (hoặc entity/)               // --- LỚP MODEL (ÁNH XẠ CSDL) --- (Mỗi class là 1 bảng)
│   │   │       │   ├── User.java                       // Đại diện bảng 'users'
│   │   │       │   ├── Court.java                      // Đại diện bảng 'courts'
│   │   │       │   ├── Booking.java                    // Đại diện bảng 'bookings'
│   │   │       │   ├── TimeSlot.java                   // Đại diện bảng 'time_slots'
│   │   │       │   ├── Payment.java                    // Đại diện bảng 'payments'
│   │   │       │   ├── Notification.java               // Đại diện bảng 'notifications'
│   │   │       │   └── BaseEntity.java                 // Lớp cha chứa (id, createdAt, updatedAt) dùng chung
│   │   │       │
│   │   │       ├── dto/                                // --- LỚP DTO (VẬN CHUYỂN DỮ LIỆU) --- (Các "hộp" chứa dữ liệu)
│   │   │       │   ├── auth/                           // DTOs cho Xác thực
│   │   │       │   │   ├── LoginRequest.java           // Dữ liệu client gửi lên khi đăng nhập
│   │   │       │   │   ├── RegisterRequest.java        // Dữ liệu client gửi lên khi đăng ký
│   │   │       │   │   └── AuthResponse.java           // Dữ liệu server trả về (chứa token)
│   │   │       │   ├── user/
│   │   │       │   │   └── UserProfileResponse.java    // Dữ liệu server trả về (thông tin user, giấu password)
│   │   │       │   ├── court/
│   │   │       │   │   ├── CourtSummaryResponse.java   // Dữ liệu tóm tắt (hiển thị danh sách)
│   │   │       │   │   └── CourtDetailResponse.java    // Dữ liệu chi tiết (khi xem 1 sân)
│   │   │       │   ├── booking/
│   │   │       │   │   ├── CreateBookingRequest.java   // Dữ liệu client gửi lên (sân nào, giờ nào)
│   │   │       │   │   └── BookingDetailResponse.java  // Dữ liệu chi tiết 1 đơn đặt sân
│   │   │       │   ├── payment/                        // DTOs cho Thanh toán
│   │   │       │   │   ├── CreatePaymentRequest.java   // Input: booking_id, payment_method
│   │   │       │   │   ├── PaymentResponse.java        // Output: Trả về payment_url (để mở MoMo/VNPay)
│   │   │       │   │   └── PaymentWebhookRequest.java  // Input: Dữ liệu từ cổng thanh toán bắn về (webhook)
│   │   │       │   ├── notification/                   // DTOs cho Thông báo
│   │   │       │   │   ├── NotificationResponse.java   // Output: Dữ liệu của 1 thông báo
│   │   │       │   │   └── SendNotificationRequest.java  // Input: Gửi thông báo (cho admin)
│   │   │       │   ├── admin/                          // DTOs cho Admin
│   │   │       │   │   ├── AdminDashboardStatsResponse.java // Output: Các số liệu (doanh thu, user, top sân)
│   │   │       │   │   ├── AdminBookingDetailResponse.java  // Output: Danh sách booking cho admin
│   │   │       │   │   └── ConfirmPaymentRequest.java     // Input: Xác nhận thanh toán (nếu cần)
│   │   │       │   └── report/                         // DTOs cho Báo cáo
│   │   │       │       ├── CreateReportRequest.java    // Input: type, params (ví dụ: "doanh thu tháng 10")
│   │   │       │       └── ReportResponse.java         // Output: Dữ liệu của báo cáo đã tạo
│   │   │       │
│   │   │       ├── security/ (hoặc jwt/)               // --- GÓI BẢO MẬT ---
│   │   │       │   ├── JwtTokenProvider.java         // "Nhà máy" tạo và giải mã JWT
│   │   │       │   ├── JwtAuthenticationFilter.java    // "Bảo vệ" chặn request, kiểm tra token
│   │   │       │   └── UserDetailsServiceImpl.java     // "Cầu nối" giúp Spring Security nói chuyện với UserRepository
│   │   │       │
│   │   │       ├── exception/                          // --- GÓI XỬ LÝ LỖI ---
│   │   │       │   ├── GlobalExceptionHandler.java     // Nơi "bắt" tất cả lỗi và trả về JSON (ApiError)
│   │   │       │   ├── ResourceNotFoundException.java  // Lỗi 404 tùy chỉnh
│   │   │       │   └── ApiError.java                   // Class định dạng cấu trúc JSON trả về khi có lỗi
│   │   │       │
│   │   │       └── common/ (hoặc util/)                // --- GÓI DÙNG CHUNG ---
│   │   │           ├── AppConstant.java                // Chứa các hằng số (ví dụ: số item/trang)
│   │   │           └── UserRole.java                   // Enum định nghĩa các vai trò (ADMIN, PLAYER, MANAGER)
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties          // Cấu hình chung, mặc định
│   │   │   ├── application-dev.properties      // Cấu hình cho môi trường dev (chạy local)
│   │   │   ├── application-prod.properties     // Cấu hình cho môi trường production (chạy thật)
│   │   │   ├── messages.properties             // Chứa các thông báo lỗi (hỗ trợ đa ngôn ngữ)
│   │   │   └── db/migration/                   // Chứa file SQL để quản lý phiên bản CSDL (dùng Flyway/Liquibase)
│   │   │       └── V1__init_schema.sql         // File SQL ban đầu để tạo các bảng
│   │   │
│   └── test/                                   // Nơi chứa code Unit Test và Integration Test
│
├── .gitignore                                  // Chỉ định các file/thư mục Git bỏ qua (ví dụ: target/)
├── pom.xml                                     // Quản lý tất cả thư viện (dependencies) của dự án
└── README.md                                   // File văn bản mô tả dự án, hướng dẫn cài đặt
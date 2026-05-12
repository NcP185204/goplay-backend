package com.backend.GoPlay.service.specification;

import com.backend.GoPlay.dto.court.CourtSearchCriteria; // Import lớp chứa các tiêu chí tìm kiếm (tên, giá, loại sân,...)
import com.backend.GoPlay.model.Court;                   // Import Entity Court để định nghĩa truy vấn
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;          // Import interface Predicate của JPA Criteria API
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification; // Import interface chính của Spring Data JPA Specification
import org.springframework.stereotype.Component;          // Import annotation đánh dấu Component
import java.util.ArrayList;                             // Import ArrayList để lưu trữ danh sách điều kiện truy vấn
import java.util.List;                                  // Import List

/**
 * CourtSpecification: Lớp xây dựng Specification (tiêu chí truy vấn) động
 * cho Entity Court dựa trên các tiêu chí tìm kiếm từ người dùng.
 */
@Component // Đánh dấu đây là một Spring Component (Service/Utility) để có thể được inject tự động
public class CourtSpecification {

    /**
     * Phương thức xây dựng Specification dựa trên CourtSearchCriteria.
     *
     * @param criteria Đối tượng chứa các tham số tìm kiếm (tên, loại, giá, vị trí).
     * @return Specification<Court> Tiêu chí truy vấn có thể dùng trong Repository.
     */
    public Specification<Court> build(CourtSearchCriteria criteria) {
        // Trả về một lambda function (Specification) định nghĩa cách xây dựng truy vấn
        return (root, query, cb) -> {
            // root: Tham chiếu đến Entity Court (tương đương với FROM Court)
            // query: Tham chiếu đến truy vấn gốc (dùng cho các lệnh JOIN, GROUP BY, ORDER BY)
            // cb: CriteriaBuilder, công cụ để xây dựng các điều kiện (Predicate) như AND, OR, LIKE, EQUAL.

            List<Predicate> predicates = new ArrayList<>(); // Danh sách chứa các điều kiện truy vấn (WHERE clauses)

            // --- 1. LỌC THEO TÊN (Case-Insensitive LIKE) ---
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                // Thêm điều kiện: tên sân phải chứa chuỗi tìm kiếm (không phân biệt chữ hoa/thường)
                predicates.add(cb.like(
                        cb.lower(root.get("name")), // Chuyển tên cột 'name' thành chữ thường
                        "%" + criteria.getName().toLowerCase() + "%" // Chuỗi tìm kiếm cũng chuyển thành chữ thường và thêm wildcard %
                ));
            }

            // --- 1.5. LỌC THEO ĐỊA CHỈ TƯƠNG ĐỐI (Thông minh xử lý viết tắt Q1, Quận 1, Q.1...) ---
            if (criteria.getAddress() != null && !criteria.getAddress().trim().isEmpty()) {
                predicates.add(buildAddressPredicate(criteria.getAddress(), root, cb));
            }

            // --- 2. LỌC THEO LOẠI SÂN (Exact Match) ---
            if (criteria.getCourtType() != null) {
                // Thêm điều kiện: courtType phải bằng giá trị đã cho
                // Enum đã được map tự động bởi Hibernate và xử lý chuẩn từ Request Parameter
                predicates.add(cb.equal(root.get("courtType"), criteria.getCourtType()));
            }

            // --- 3. LỌC THEO GIÁ TỐI THIỂU ---
            if (criteria.getMinPrice() != null) {
                // Thêm điều kiện: pricePerHour >= minPrice
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerHour"), criteria.getMinPrice()));
            }

            // --- 4. LỌC THEO GIÁ TỐI ĐA ---
            if (criteria.getMaxPrice() != null) {
                // Thêm điều kiện: pricePerHour <= maxPrice
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerHour"), criteria.getMaxPrice()));
            }

            // --- 5. LỌC THEO ĐÁNH GIÁ TỐI THIỂU ---
            if (criteria.getMinRating() != null) {
                // Thêm điều kiện: averageRating >= minRating
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), criteria.getMinRating()));
            }

            // --- 6. LỌC VỊ TRÍ SƠ BỘ (Bounding box/Khung giới hạn) ---
            if (criteria.getLatitude() != null && criteria.getLongitude() != null && criteria.getRadiusInKm() != null) {
                double lat = criteria.getLatitude();
                double lon = criteria.getLongitude();
                double radiusKm = criteria.getRadiusInKm();

                // Tính toán độ lệch vĩ độ (latitude) và kinh độ (longitude) tương ứng với bán kính
                // Giả định: 1 độ vĩ độ xấp xỉ 111 km.
                double degreeLat = radiusKm / 111.0;
                // Tính độ lệch kinh độ, có tính đến sự thay đổi theo vĩ độ (dùng cos)
                double degreeLon = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

                // Thêm điều kiện: Vĩ độ của sân nằm trong khoảng (lat - degreeLat) và (lat + degreeLat)
                predicates.add(cb.between(root.get("latitude"), lat - degreeLat, lat + degreeLat));
                // Thêm điều kiện: Kinh độ của sân nằm trong khoảng (lon - degreeLon) và (lon + degreeLon)
                predicates.add(cb.between(root.get("longitude"), lon - degreeLon, lon + degreeLon));
            }

            // --- KẾT HỢP CÁC ĐIỀU KIỆN ---
            // Kết hợp tất cả các Predicate trong danh sách bằng toán tử AND (VÀ)
            // Nếu danh sách rỗng, truy vấn sẽ không có điều kiện WHERE.
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Hàm hỗ trợ xử lý logic tìm kiếm địa chỉ thông minh.
     * Tự động sinh ra các biến thể viết tắt của Quận (Q1, Quận 1, Q.1, Q 1).
     */
    private Predicate buildAddressPredicate(String addressInput, Root<Court> root, CriteriaBuilder cb) {
        String lowerInput = addressInput.toLowerCase().trim();
        List<String> searchTerms = new ArrayList<>();
        
        // Xử lý từ khóa "Q" kèm theo số (VD: q1, q2, q12)
        if (lowerInput.matches("^q\\s*\\d+$")) {
            String number = lowerInput.replace("q", "").trim();
            searchTerms.add("quận " + number);
            searchTerms.add("q." + number);
            searchTerms.add("q " + number);
            searchTerms.add("q" + number);
        } 
        // Xử lý từ khóa "Quận" kèm theo số (VD: quận 1, quận 12)
        else if (lowerInput.matches("^quận\\s*\\d+$")) {
            String number = lowerInput.replace("quận", "").trim();
            searchTerms.add("q" + number);
            searchTerms.add("q." + number);
            searchTerms.add("q " + number);
            searchTerms.add("quận " + number);
        }
        // Xử lý Huyện (VD: h.bình chánh, huyện bình chánh)
        else if (lowerInput.startsWith("h ")) {
            searchTerms.add("huyện " + lowerInput.substring(2).trim());
        } else if (lowerInput.startsWith("huyện ")) {
            searchTerms.add("h " + lowerInput.substring(6).trim());
        } else {
            // Nếu không phải các dạng trên, chỉ tìm chuỗi gốc
            searchTerms.add(lowerInput);
        }

        // Tạo danh sách các điều kiện OR
        List<Predicate> orPredicates = new ArrayList<>();
        for (String term : searchTerms) {
            // SỬA LỖI: Thêm khoảng trắng hoặc dấu phẩy để đảm bảo tìm đúng từ
            // Ví dụ: Tìm " q1," hoặc " q1 " hoặc " q1."
            orPredicates.add(cb.like(cb.lower(root.get("address")), "% " + term + ",%"));
            orPredicates.add(cb.like(cb.lower(root.get("address")), "% " + term + " %"));
            orPredicates.add(cb.like(cb.lower(root.get("address")), "%," + term + ",%"));
            orPredicates.add(cb.like(cb.lower(root.get("address")), "%," + term + " %"));
        }

        // Kết hợp chúng lại bằng toán tử OR (Hoặc chứa " q1," hoặc " quận 1 "...)
        return cb.or(orPredicates.toArray(new Predicate[0]));
    }
}

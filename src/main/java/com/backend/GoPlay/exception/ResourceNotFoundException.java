package com.backend.GoPlay.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoại lệ tùy chỉnh cho các trường hợp không tìm thấy tài nguyên (lỗi 404).
 * Ví dụ: Không tìm thấy sân, không tìm thấy user theo ID, v.v.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Nói với Spring trả về mã HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor đơn giản chỉ nhận thông báo lỗi.
     * @param message Thông báo lỗi cụ thể.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor mở rộng (ít dùng hơn), nhận thêm ID và tên trường
     * Ví dụ: new ResourceNotFoundException("Court", "id", 10);
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s không được tìm thấy với %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
package com.backend.GoPlay.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * Lưu một file và trả về đường dẫn có thể truy cập.
     * @param file File được upload.
     * @param subfolder Thư mục con để lưu file (ví dụ: "courts", "avatars").
     * @return Đường dẫn tương đối của file đã lưu.
     */
    String store(MultipartFile file, String subfolder);

    /**
     * Xóa một file dựa trên đường dẫn của nó.
     * @param fileUrl Đường dẫn của file cần xóa.
     */
    void delete(String fileUrl);
}

package com.backend.GoPlay.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Thư mục gốc để lưu tất cả các file upload
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            // Tạo thư mục gốc nếu nó chưa tồn tại
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Lưu một file vào một thư mục con cụ thể.
     * @param file File được upload.
     * @param subfolder Thư mục con (ví dụ: "courts", "avatars").
     * @return Đường dẫn tương đối của file đã lưu (ví dụ: "/uploads/courts/filename.jpg").
     */
    public String store(MultipartFile file, String subfolder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            // Tạo đường dẫn đến thư mục con
            Path destinationFolder = rootLocation.resolve(Paths.get(subfolder))
                    .normalize().toAbsolutePath();

            // Kiểm tra bảo mật để đảm bảo file không được lưu bên ngoài thư mục gốc
            if (!destinationFolder.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            // Tạo thư mục con nếu chưa tồn tại
            Files.createDirectories(destinationFolder);

            // Tạo tên file duy nhất để tránh trùng lặp
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Sao chép file vào thư mục đích
            try (InputStream inputStream = file.getInputStream()) {
                Path destinationFile = destinationFolder.resolve(uniqueFileName);
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Trả về đường dẫn URL tương đối để lưu vào database
            return "/" + rootLocation.getFileName().toString() + "/" + subfolder + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    /**
     * Xóa một file dựa trên URL tương đối của nó.
     * @param fileUrl Đường dẫn URL của file (ví dụ: "/uploads/courts/filename.jpg").
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // Chuyển đổi từ URL sang đường dẫn hệ thống file
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path file = rootLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Ghi log lỗi thay vì ném exception để không làm gián đoạn các thao tác khác
            System.err.println("Failed to delete file: " + fileUrl);
        }
    }
}

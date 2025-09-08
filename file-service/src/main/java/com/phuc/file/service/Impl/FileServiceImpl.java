package com.phuc.file.service.Impl;

import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.entity.File;
import com.phuc.file.exception.ResourceNotFoundException;
import com.phuc.file.mapper.FileMapper;
import com.phuc.file.repository.FileRepository;
import com.phuc.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    FileRepository fileRepository;
    FileMapper fileMapper;

    private final String UPLOAD_DIR = "uploads"; // Bạn có thể đặt trong application.properties

    @Override
    public FileResponse uploadFile(MultipartFile multipartFile) throws IOException {
        // Tạo thư mục nếu chưa có
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFileName = UUID.randomUUID() + fileExtension;

        Path filePath = uploadPath.resolve(storedFileName);
        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Lưu thông tin vào DB
        File file = File.builder()
                .name(originalFilename)
                .type(multipartFile.getContentType())
                .url(filePath.toString()) // hoặc sinh URL phục vụ nếu dùng web server
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        fileRepository.save(file);

        return fileMapper.toFileResponse(file);
    }

    @Override
    public List<FileResponse> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        return files.stream()
                .map(file -> {
                    try {
                        return uploadFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload: " + file.getOriginalFilename(), e);
                    }
                }).toList();
    }

    @Override
    public void deleteFile(String fileName) {
        // Xóa file vật lý
        Path path = Paths.get(UPLOAD_DIR).resolve(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + fileName, e);
        }

        // Xóa khỏi DB
        File file = fileRepository.findByName(fileName)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileName));
        fileRepository.delete(file);
    }

    @Override
    public FileResponse getFile(String fileName) {
        File file = fileRepository.findByName(fileName)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileName));
        return fileMapper.toFileResponse(file);
    }
}

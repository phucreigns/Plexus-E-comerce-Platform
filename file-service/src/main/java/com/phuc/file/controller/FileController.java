package com.phuc.file.controller;

import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.service.FileService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        FileResponse response = fileService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<FileResponse> responses = fileService.uploadMultipleFiles(files);
        return ResponseEntity.ok(responses);
    }

    // 🧾 Lấy thông tin file theo tên
    @GetMapping("/{fileName}")
    public ResponseEntity<FileResponse> getFile(@PathVariable String fileName) {
        FileResponse response = fileService.getFile(fileName);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }
}

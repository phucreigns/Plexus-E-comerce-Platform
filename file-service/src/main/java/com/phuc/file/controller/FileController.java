package com.phuc.file.controller;

import com.phuc.file.dto.ApiResponse;
import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        FileResponse response = fileService.uploadFile(file);
        return ApiResponse.<FileResponse>builder()
                .result(response)
                .message("File uploaded successfully")
                .build();
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<FileResponse>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files) throws IOException {
        List<FileResponse> responses = fileService.uploadMultipleFiles(files);
        return ApiResponse.<List<FileResponse>>builder()
                .result(responses)
                .message("Files uploaded successfully")
                .build();
    }

    @GetMapping("/{fileName}")
    public ApiResponse<FileResponse> getFile(@PathVariable String fileName) {
        FileResponse response = fileService.getFile(fileName);
        return ApiResponse.<FileResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/{fileName}")
    public ApiResponse<String> deleteFile(@PathVariable String fileName) {
        fileService.deleteFile(fileName);
        return ApiResponse.<String>builder()
                .result("File deleted successfully")
                .message("File deleted successfully")
                .build();
    }
}

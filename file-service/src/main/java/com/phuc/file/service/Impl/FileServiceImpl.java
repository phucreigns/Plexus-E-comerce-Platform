package com.phuc.file.service.Impl;

import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.entity.File;
import com.phuc.file.mapper.FileMapper;
import com.phuc.file.repository.FileRepository;
import com.phuc.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import com.phuc.file.exception.AppException;
import com.phuc.file.exception.ErrorCode;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {

      S3Client s3Client;
      FileMapper fileMapper;
      FileRepository fileRepository;

      @Value("${cloud.aws.s3.bucket}")
      @NonFinal
      String bucketName;

      @Value("${cloud.aws.region.static}")
      @NonFinal
      String region;

      @Override
      public FileResponse uploadFile(MultipartFile file) throws IOException {
            if (file == null || file.isEmpty()) {
                  throw new AppException(ErrorCode.MISSING_REQUIRED_PARAMETER);
            }

            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename();

            uploadToS3(file, fileName);

            String fileUrl = generateFileUrl(fileName);
            File fileEntity = saveFileMetadata(file, fileName, fileUrl);

            return fileMapper.toFileResponse(fileEntity);
      }

      @Override
      public List<FileResponse> uploadMultipleFiles(List<MultipartFile> files) {
            if (files == null || files.isEmpty()) {
                  throw new AppException(ErrorCode.MISSING_REQUIRED_PARAMETER);
            }

            List<FileResponse> responses = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();

            files.parallelStream().forEach(file -> {    
                  if (file == null || file.isEmpty()) {
                        synchronized (failedFiles) {
                              failedFiles.add(file != null ? file.getOriginalFilename() : "null");
                        }
                        return;
                  }

                  try {
                        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename();

                        uploadToS3(file, fileName);

                        String fileUrl = generateFileUrl(fileName);
                        File fileEntity = saveFileMetadata(file, fileName, fileUrl);

                        synchronized (responses) {
                              responses.add(fileMapper.toFileResponse(fileEntity));
                        }
                  } catch (IOException e) {
                        log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                        synchronized (failedFiles) {
                              failedFiles.add(file.getOriginalFilename());
                        }
                  }
            });

            if (!failedFiles.isEmpty()) {
                  log.warn("Some files failed to upload: {}", failedFiles);
            }

            if (responses.isEmpty() && !failedFiles.isEmpty()) {
                  throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            return responses;
      }

      @Override
      public void deleteFile(String fileName) {
            s3Client.deleteObject(deleteRequest -> deleteRequest.bucket(bucketName).key(fileName));
            fileRepository.deleteByName(fileName);
      }

      @Override
      public FileResponse getFile(String fileName) {
            File fileEntity = fileRepository.findByName(fileName)
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            return fileMapper.toFileResponse(fileEntity);
      }

      private String generateFileUrl(String fileName) {
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
      }

      private void uploadToS3(MultipartFile file, String fileName) throws IOException {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
      }

      private File saveFileMetadata(MultipartFile file, String fileName, String fileUrl) {
            File fileEntity = File.builder()
                    .fileId(UUID.randomUUID().toString())
                    .name(fileName)
                    .url(fileUrl)
                    .size(String.valueOf(file.getSize()))
                    .type(file.getContentType())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            File savedFile = fileRepository.save(fileEntity);
            return savedFile;
      }

}
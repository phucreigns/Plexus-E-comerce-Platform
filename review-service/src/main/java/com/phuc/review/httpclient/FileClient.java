package com.phuc.review.httpclient;

import com.phuc.review.configuration.AuthenticationRequestInterceptor;
import com.phuc.review.dto.ApiResponse;
import com.phuc.review.httpclient.response.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "file-service", url = "${file.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {

    @PostMapping(value = "/upload-multiple", consumes = "multipart/form-data", produces = "application/json")
    ApiResponse<List<FileResponse>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files);

}























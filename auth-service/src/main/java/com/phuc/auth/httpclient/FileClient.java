package com.phuc.auth.httpclient;

import com.phuc.auth.configuration.AuthenticationRequestInterceptor;
import com.phuc.auth.httpclient.response.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "file-service", url = "${file.service.url}", configuration = AuthenticationRequestInterceptor.class)
public interface FileClient {

    @PostMapping("/upload")
    ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file);
}

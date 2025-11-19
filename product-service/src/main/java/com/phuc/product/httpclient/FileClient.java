package com.phuc.product.httpclient;

import com.phuc.product.configuration.AuthenticationRequestInterceptor;
import com.phuc.product.dto.ApiResponse;
import com.phuc.product.httpclient.response.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "file-service", url = "${file.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<FileResponse>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files);

}
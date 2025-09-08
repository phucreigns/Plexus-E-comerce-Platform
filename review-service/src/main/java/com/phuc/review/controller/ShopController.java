package com.phuc.shop.controller;

import com.phuc.shop.dto.request.ShopCreateRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.service.ShopService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopController {
    ShopService shopService;

    @PostMapping("/create")
    public ResponseEntity<ShopResponse> createShop(@RequestBody ShopCreateRequest request) {
        ShopResponse response = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{shopId}")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable Long shopId,
            @RequestBody ShopUpdateRequest request) {
        ShopResponse response = shopService.updateShop(request, shopId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<ShopResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        ShopResponse response = shopService.uploadLogo(file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long shopId) {
        shopService.deleteShop(shopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        List<ShopResponse> shops = shopService.searchShops();
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getByShopId(@PathVariable Long shopId) {
        ShopResponse response = shopService.getByShopId(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ShopResponse> getByShopName(@PathVariable String name) {
        ShopResponse response = shopService.getByShopName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-report")
    public ResponseEntity<SalesReportResponse> generateSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        SalesReportResponse report = shopService.generateSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/my-sales-report")
    public ResponseEntity<SalesReportResponse> getMySalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        SalesReportResponse report = shopService.getMySalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }


}

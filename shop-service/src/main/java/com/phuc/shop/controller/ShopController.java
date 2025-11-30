package com.phuc.shop.controller;

import com.phuc.shop.dto.ApiResponse;
import com.phuc.shop.dto.request.ShopCreationRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopController {
    ShopService shopService;

    @PostMapping("/create")
    public ApiResponse<ShopResponse> createShop(@RequestBody @Valid ShopCreationRequest request) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.createShop(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<ShopResponse> updateShop(@RequestBody @Valid ShopUpdateRequest request) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.updateShop(request))
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteShop() {
        shopService.deleteShop();
        return ApiResponse.<Void>builder()
                .message("Shop deleted successfully")
                .build();
    }

    @GetMapping("/{shopId}/sales-report")
    public ApiResponse<SalesReportResponse> generateSalesReport(@PathVariable("shopId") String shopId,
                                                                @RequestParam("startDate") String startDate,
                                                                @RequestParam("endDate") String endDate) {
        return ApiResponse.<SalesReportResponse>builder()
                .result(shopService.generateSalesReport(shopId, startDate, endDate))
                .build();
    }

    @GetMapping("/my-sales-report")
    public ApiResponse<SalesReportResponse> getMySalesReport(@RequestParam("startDate") String startDate,
                                                             @RequestParam("endDate") String endDate) {
        return ApiResponse.<SalesReportResponse>builder()
                .result(shopService.getMySalesReport(startDate, endDate))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<ShopResponse>> searchShops(@RequestParam("keyword") String keyword,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ShopResponse>>builder()
                .result(shopService.searchShops(keyword, page, size))
                .build();
    }

    @GetMapping("/owner/{email}")
    public ApiResponse<ShopResponse> getShopByOwnerEmail(@PathVariable("email") String email) {
        // Nếu DB lưu dạng "auth0|xxxx", email sẽ chính xác thay vì "auth0%7Cxxxx"
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.getShopByOwnerEmail(email))
                .build();
    }


    @GetMapping("/{shopId}/owner")
    public ApiResponse<String> getOwnerEmailByShopId(@PathVariable("shopId") String shopId) {
        return ApiResponse.<String>builder()
                .result(shopService.getOwnerEmailByShopId(shopId))
                .build();
    }

    @GetMapping("/{shopId}/exists")
    public ApiResponse<Boolean> checkIfShopExists(@PathVariable("shopId") String shopId) {
        return ApiResponse.<Boolean>builder()
                .result(shopService.checkIfShopExists(shopId))
                .build();
    }

}
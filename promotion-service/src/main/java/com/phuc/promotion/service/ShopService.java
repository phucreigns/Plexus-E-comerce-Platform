package com.phuc.promotion.service;

import com.phuc.shop.dto.request.ShopCreateRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ShopService {
    ShopResponse createShop(ShopCreateRequest request);
    ShopResponse uploadLogo(MultipartFile file);

    ShopResponse updateShop(ShopUpdateRequest request, Long shopId);

    void deleteShop(Long shopId);
    List<ShopResponse> searchShops();
    ShopResponse getByShopId(Long shopId);
    ShopResponse getByShopName(String name);
    SalesReportResponse generateSalesReport(String startDate, String endDate);
    SalesReportResponse getMySalesReport(String startDate, String endDate);
}


package com.phuc.shop.service;

import com.phuc.shop.dto.request.ShopCreationRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import org.springframework.data.domain.Page;

public interface ShopService {

    ShopResponse createShop(ShopCreationRequest request);

    ShopResponse updateShop(ShopUpdateRequest request);

    void deleteShop();

    SalesReportResponse generateSalesReport(String shopId, String startDate, String endDate);

    SalesReportResponse getMySalesReport(String startDate, String endDate);

    Page<ShopResponse> searchShops(String keyword, int page, int size);

    ShopResponse getShopByOwnerEmail(String email);

    String getOwnerEmailByShopId(String shopId);

    boolean checkIfShopExists(String shopId);

}
package com.phuc.shop.service.Impl;

import com.phuc.shop.dto.request.ShopCreationRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.entity.Shop;
import com.phuc.shop.exception.AppException;
import com.phuc.shop.exception.ErrorCode;
import com.phuc.shop.httpclient.OrderClient;
import com.phuc.shop.httpclient.ProductClient;
import com.phuc.shop.httpclient.response.OrderItemResponse;
import com.phuc.shop.httpclient.response.OrderResponse;
import com.phuc.shop.httpclient.response.ProductResponse;
import com.phuc.shop.mapper.ShopMapper;
import com.phuc.shop.repository.ShopRepository;
import com.phuc.shop.service.ShopService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    ProductClient productClient;
    OrderClient orderClient;
    ShopMapper shopMapper;

    @Override
    @Transactional
    public ShopResponse createShop(ShopCreationRequest request) {
        String email = getCurrentEmail();

        if (shopRepository.existsByOwnerEmail(email)) {
            log.error("User {} already has a shop", email);
            throw new AppException(ErrorCode.ALREADY_HAVE_A_SHOP);
        }

        Shop shop = shopMapper.toShop(request);
        shop.setOwnerEmail(email);
        shop.setEmail(email);
        shop = shopRepository.save(shop);

        return shopMapper.toShopResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(ShopUpdateRequest request) {
        String currentEmail = getCurrentEmail();
        Shop shop = shopRepository.findByOwnerEmail(currentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        shopMapper.updateShop(shop, request);
        return shopMapper.toShopResponse(shopRepository.save(shop));
    }

    @Override
    @Transactional
    public void deleteShop() {
        String currentEmail = getCurrentEmail();
        Shop shop = shopRepository.findByOwnerEmail(currentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        shopRepository.delete(shop);
    }

    @Override
    @Transactional
    public SalesReportResponse generateSalesReport(String shopId, String startDate, String endDate) {
        String formattedStartDate = formatDateForOrderService(startDate);
        String formattedEndDate = formatDateForOrderService(endDate);

        List<OrderResponse> orders;
        try {
            orders = orderClient.getOrdersByDateRange(formattedStartDate, formattedEndDate);
        } catch (FeignException e) {
            log.error("Error fetching orders for date range {} to {}: {}", formattedStartDate, formattedEndDate, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        if (orders == null || orders.isEmpty()) {
            return new SalesReportResponse(
                    0.0,
                    0,
                    Collections.emptyList(),
                    "No Orders",
                    0.0,
                    "No Orders",
                    startDate, endDate
            );
        }

        double totalRevenue = 0.0;
        int totalItemsSold = 0;
        Map<String, Integer> productSalesCount = new HashMap<>();
        Map<String, Double> productRevenueMap = new HashMap<>();
        String topSellingProduct = "";
        int highestSoldQuantity = 0;
        double highestRevenueProductRevenue = 0.0;
        String highestRevenueProduct = "";

        for (OrderResponse order : orders) {
            if (order.getItems() == null || order.getItems().isEmpty()) {
                continue;
            }

            for (OrderItemResponse item : order.getItems()) {
                try {
                    String productShopId = productClient.getShopIdByProductId(item.getProductId()).getResult();
                    
                    if (!shopId.equals(productShopId)) {
                        continue;
                    }

                    ProductResponse product = productClient.getProductById(item.getProductId()).getResult();
                    if (product == null) {
                        continue;
                    }

                    Double price = productClient.getProductPriceById(item.getProductId(), item.getVariantId()).getResult();
                    if (price == null) {
                        continue;
                    }

                    double itemRevenue = price * item.getQuantity();
                    totalRevenue += itemRevenue;
                    totalItemsSold += item.getQuantity();

                    String productName = product.getName();
                    productSalesCount.put(productName, productSalesCount.getOrDefault(productName, 0) + item.getQuantity());
                    productRevenueMap.put(productName, productRevenueMap.getOrDefault(productName, 0.0) + itemRevenue);

                } catch (FeignException e) {
                    log.warn("Error fetching product info for productId {}: {}", item.getProductId(), e.getMessage());
                }
            }
        }

        if (!productSalesCount.isEmpty()) {
            for (Map.Entry<String, Integer> entry : productSalesCount.entrySet()) {
                String productName = entry.getKey();
                int quantity = entry.getValue();
                double revenue = productRevenueMap.getOrDefault(productName, 0.0);

                if (quantity > highestSoldQuantity) {
                    highestSoldQuantity = quantity;
                    topSellingProduct = productName;
            }

                if (revenue > highestRevenueProductRevenue) {
                    highestRevenueProductRevenue = revenue;
                    highestRevenueProduct = productName;
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedProductSales = productSalesCount.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .toList();

        return new SalesReportResponse(
                totalRevenue,
                totalItemsSold,
                sortedProductSales,
                topSellingProduct.isEmpty() ? "No Orders" : topSellingProduct,
                highestRevenueProductRevenue,
                highestRevenueProduct.isEmpty() ? "No Orders" : highestRevenueProduct,
                startDate,
                endDate
        );
    }

    @Override
    public SalesReportResponse getMySalesReport(String startDate, String endDate) {
        String email = getCurrentEmail();
        log.info("Getting sales report for user: {}", email);
        
        Shop shop = shopRepository.findByOwnerEmail(email)
                .orElseThrow(() -> {
                    log.error("Shop not found for owner email: {}", email);
                    return new AppException(ErrorCode.SHOP_NOT_FOUND);
                });

        log.info("Found shop: {} (id: {}) for owner: {}", shop.getName(), shop.getId(), email);
        
        return generateSalesReport(shop.getId(), startDate, endDate);
    }

    @Override
    public Page<ShopResponse> searchShops(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Shop> shops = shopRepository.searchShops(keyword, pageRequest);
        return shops.map(shopMapper::toShopResponse);
    }

    @Override
    public ShopResponse getShopByOwnerEmail(String email) {
        Shop shop = shopRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        return shopMapper.toShopResponse(shop);
    }

    @Override
    public String getOwnerEmailByShopId(String shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND))
                .getOwnerEmail();
    }

    @Override
    public boolean checkIfShopExists(String shopId) {
        return shopRepository.existsById(shopId);
    }

    private String getCurrentEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("email");
    }

    private String formatDateForOrderService(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_PARAMETER);
        }

        try {
            dateStr = dateStr.replaceAll("T+", "T");
            
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr + "T00:00:00";
            }
            
            LocalDateTime dateTime;
            
            if (dateStr.contains("T")) {
                String[] parts = dateStr.split("T");
                if (parts.length == 2) {
                    String datePart = parts[0];
                    String timePart = parts[1];
                    
                    if (timePart.contains(".")) {
                        timePart = timePart.substring(0, timePart.indexOf("."));
                    }
                    
                    String[] timeParts = timePart.split(":");
                    if (timeParts.length == 1) {
                        timePart = timePart + ":00:00";
                    } else if (timeParts.length == 2) {
                        timePart = timePart + ":00";
                    }
                    
                    dateStr = datePart + "T" + timePart;
                }
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                dateTime = LocalDateTime.parse(dateStr, formatter);
            } else if (dateStr.contains(" ")) {
                dateStr = dateStr.replace(" ", "T");
                if (dateStr.contains(".")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf("."));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                dateTime = LocalDateTime.parse(dateStr, formatter);
            } else {
                dateTime = LocalDateTime.parse(dateStr + "T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
            
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            log.error("Error formatting date {}: {}", dateStr, e.getMessage());
            throw new AppException(ErrorCode.MISSING_REQUIRED_PARAMETER);
        }
    }

}
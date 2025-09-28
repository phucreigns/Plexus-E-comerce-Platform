package com.phuc.shop.service.Impl;

import com.phuc.shop.dto.request.ShopCreationRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.entity.Shop;
import com.phuc.shop.exception.AppException;
import com.phuc.shop.exception.ErrorCode;
import com.phuc.shop.httpclient.ProductClient;
import com.phuc.shop.httpclient.response.ProductResponse;
import com.phuc.shop.httpclient.response.ProductVariantResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    ProductClient productClient;
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
        List<ProductResponse> products;
        try {
            products = productClient.getProductsByShopId(shopId).getResult();
        } catch (FeignException e) {
            log.error("Error fetching products for shopId {}: {}", shopId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        if (products == null || products.isEmpty()) {
            return new SalesReportResponse(
                    0.0,
                    0,
                    Collections.emptyList(),
                    "No Product",
                    0.0,
                    "No Product",
                    startDate, endDate
            );
        }

        double totalRevenue = 0.0;
        int totalItemsSold = 0;
        Map<String, Integer> productSalesCount = new HashMap<>();
        String topSellingProduct = "";
        int highestSoldQuantity = 0;
        double highestRevenueProductRevenue = 0.0;
        String highestRevenueProduct = "";

        for (ProductResponse product : products) {
            double productRevenue = 0.0;
            int productTotalSold = 0;

            for (ProductVariantResponse variant : product.getVariants()) {
                int soldQuantity = variant.getSoldQuantity();
                double price = variant.getPrice();
                productRevenue += price * soldQuantity;
                productTotalSold += soldQuantity;
            }

            totalRevenue += productRevenue;
            totalItemsSold += productTotalSold;
            productSalesCount.put(product.getName(), productTotalSold);

            if (productTotalSold > highestSoldQuantity) {
                highestSoldQuantity = productTotalSold;
                topSellingProduct = product.getName();
            }

            if (productRevenue > highestRevenueProductRevenue) {
                highestRevenueProductRevenue = productRevenue;
                highestRevenueProduct = product.getName();
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
                topSellingProduct,
                highestRevenueProductRevenue,
                highestRevenueProduct,
                startDate,
                endDate
        );
    }

    @Override
    public SalesReportResponse getMySalesReport(String startDate, String endDate) {
        String email = getCurrentEmail();
        Shop shop = shopRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        List<ProductResponse> products;
        try {
            products = productClient.getProductsByShopId(shop.getId()).getResult();
        } catch (FeignException e) {
            log.error("(User) Error fetching products for shopId {}: {}", shop.getId(), e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        if (products == null || products.isEmpty()) {
            return new SalesReportResponse(
                    0.0,
                    0,
                    Collections.emptyList(),
                    "No Product",
                    0.0,
                    "No Product",
                    startDate, endDate
            );
        }

        double totalRevenue = 0.0;
        int totalItemsSold = 0;
        Map<String, Integer> productSalesCount = new HashMap<>();
        String topSellingProduct = "";
        int highestSoldQuantity = 0;
        double highestRevenueProductRevenue = 0.0;
        String highestRevenueProduct = "";

        for (ProductResponse product : products) {
            double productRevenue = 0.0;
            int productTotalSold = 0;

            for (ProductVariantResponse variant : product.getVariants()) {
                int soldQuantity = variant.getSoldQuantity();
                double price = variant.getPrice();
                productRevenue += price * soldQuantity;
                productTotalSold += soldQuantity;
            }

            totalRevenue += productRevenue;
            totalItemsSold += productTotalSold;
            productSalesCount.put(product.getName(), productTotalSold);

            if (productTotalSold > highestSoldQuantity) {
                highestSoldQuantity = productTotalSold;
                topSellingProduct = product.getName();
            }

            if (productRevenue > highestRevenueProductRevenue) {
                highestRevenueProductRevenue = productRevenue;
                highestRevenueProduct = product.getName();
            }
        }

        List<Map.Entry<String, Integer>> sortedProductSales = productSalesCount.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .collect(Collectors.toList());

        return new SalesReportResponse(
                totalRevenue,
                totalItemsSold,
                sortedProductSales,
                topSellingProduct,
                highestRevenueProductRevenue,
                highestRevenueProduct,
                startDate,
                endDate
        );
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


}
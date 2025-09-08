package com.phuc.shop.service.Impl;

import com.phuc.shop.dto.request.ShopCreateRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.SalesReportResponse;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.entity.Shop;
import com.phuc.shop.mapper.ShopMapper;
import com.phuc.shop.repository.ShopRepository;
import com.phuc.shop.service.ShopService;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import static lombok.AccessLevel.PRIVATE;

@Service
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {
    ShopRepository shopRepository;
    ShopMapper shopMapper;

    @Override
    public ShopResponse createShop(ShopCreateRequest request) {
        Shop shop = shopMapper.toShop(request);
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);
        return shopMapper.toShopResponse(shop);
    }

    @Override
    public ShopResponse updateShop(ShopUpdateRequest request, Long shopId) {
        Shop shop = shopRepository.findByShopId(shopId);
        shopMapper.updateShop(shop, request);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);
        return shopMapper.toShopResponse(shop);
    }

    @Override
    public ShopResponse uploadLogo(MultipartFile file) {
        // Tạm thời giả lập logic upload file
        String fakeLogoUrl = "https://your-cdn.com/upload/" + file.getOriginalFilename();
        Long shopId = 1L; // Giả lập, sau này lấy từ SecurityContext
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        shop.setLogoUrl(fakeLogoUrl);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);
        return shopMapper.toShopResponse(shop);
    }


    @Override
    public void deleteShop(Long shopId) {
        shopRepository.deleteById(shopId);
    }

    @Override
    public List<ShopResponse> searchShops() {
        return shopRepository.findAll()
                .stream()
                .map(shopMapper::toShopResponse)
                .toList();
    }

    @Override
    public ShopResponse getByShopId(Long shopId) {
        Shop shop = shopRepository.findByShopId(shopId);
        return shopMapper.toShopResponse(shop);
    }

    @Override
    public ShopResponse getByShopName(String name) {
        Shop shop = shopRepository.findByName(name);
        return shopMapper.toShopResponse(shop);
    }

    @Override
    public SalesReportResponse generateSalesReport(String startDate, String endDate) {
        return new SalesReportResponse("Sales from " + startDate + " to " + endDate);
    }

    @Override
    public SalesReportResponse getMySalesReport(String startDate, String endDate) {
        return null;
    }
}

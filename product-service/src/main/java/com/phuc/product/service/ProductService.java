package com.phuc.product.service;

import com.phuc.product.dto.request.ProductCreationRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ExistsResponse;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.enums.ProductSort;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

  ProductResponse createProduct(ProductCreationRequest request, List<MultipartFile> productImages);

  ProductResponse updateProduct(String productId, ProductUpdateRequest request, List<MultipartFile> productImages);

  void deleteProduct(String productId);

  Page<ProductResponse> getProducts(String shopId, String categoryId, int page, int size, String sortBy, String sortDirection, Double minPrice, Double maxPrice, ProductSort productSort);

  ProductResponse getProductById(String productId);

  List<ProductResponse> getProductsByShopId(String shopId);

  List<ProductResponse> getAllProducts();

  double getProductPriceById(String productId, String variantId);

  int getProductStockById(String productId, String variantId);

  ExistsResponse existsProduct(String productId, String variantId);

  String getShopIdByProductId(String productId);

  boolean isProductExist(String productId);

  List<ProductResponse> getProductsByCategoryId(String categoryId);
  void reduceStock(String productId, String variantId, Integer quantity);
  
  void restoreStock(String productId, String variantId, Integer quantity);
  
  void updateStockAndSoldQuantity(String productId, String variantId, Integer quantity);

}
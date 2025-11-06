package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.ProductRequestDto;
import com.gn.pharmacy.dto.response.BulkUploadResponse;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto requestDto) throws Exception;

    ProductResponseDto getProduct(Long productId);

    // ========== UPDATED: Changed return type to Page ==========
    Page<ProductResponseDto> getAllProducts(int page, int size);
    // ========== END UPDATED ========== //

    // ========== ADDED: New method for get by category ========== //
    List<ProductResponseDto> getProductsByCategory(String category);
    // ========== END ADDED ========== //

    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) throws Exception;

    ProductResponseDto patchProduct(Long id, ProductRequestDto requestDto) throws Exception;

    void deleteProduct(Long productId);

    List<ProductResponseDto> getProductsBySubCategory(String subCategory);

    BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception;


    List<ProductResponseDto> getProductsByCategoryPath(List<String> path);
    List<ProductResponseDto> getProductsBySubPath(String subPath); // e.g., "Chronic Care"
}
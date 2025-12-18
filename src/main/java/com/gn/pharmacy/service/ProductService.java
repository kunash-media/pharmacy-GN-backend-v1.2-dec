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

    Page<ProductResponseDto> getAllProducts(int page, int size);

    List<ProductResponseDto> getProductsByCategory(String category);

    List<ProductResponseDto> getProductsBySubCategory(String subCategory);

    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) throws Exception;

    ProductResponseDto patchProduct(Long id, ProductRequestDto requestDto) throws Exception;

    void deleteProduct(Long productId);

    BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception;

    List<ProductResponseDto> getProductsByCategoryPath(List<String> path);

    List<ProductResponseDto> getProductsBySubPath(String subPath);
}
package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.MbPRequestDto;
import com.gn.pharmacy.dto.response.MbPResponseDto;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.MbPEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MbPService {
    MbPResponseDto createMbProduct(MbPRequestDto dto);
    MbPResponseDto updateMbProduct(Long id, MbPRequestDto dto);
    MbPResponseDto patchMbProduct(Long id, MbPRequestDto dto);
    MbPResponseDto getMbProductById(Long id);
    MbPResponseDto getMbProductBySku(String sku);
    List<MbPResponseDto> getAllMbProduct();

    @Transactional(readOnly = true)
    Page<MbPResponseDto> getAllProducts(Pageable pageable);

    @Transactional(readOnly = true)
    List<MbPResponseDto> getAllProducts();

    List<MbPResponseDto> getMbProductsByCategory(String category);
    List<MbPResponseDto> getMbProductsBySubCategory(String subCategory);
    List<MbPResponseDto> searchMbProducts(String keyword);
    boolean existsBySku(String sku);
    void deleteMbProduct(Long id);
    MbPResponseDto toDto(MbPEntity entity);



//    public Page<MbPResponseDto> getAllProducts(Pageable pageable);
//    public List<MbPResponseDto> getAllProducts();


    //============= NEW METHOD ADDED=========//
    /**
     * Get ALL MB products (including deleted and non-approved)
     * With pagination support
     *
     * @return Page of all MB products with pagination metadata
     */
    Page<MbPResponseDto> getAllMbProduct(Pageable pageable);

    /**
     * Get all active MB products (non-deleted and approved only)
     * With pagination support
     *
     * @param pageable pagination information
     * @return Page of active MB products with pagination metadata
     */
    Page<MbPResponseDto> getAllActiveProducts(Pageable pageable);

    /**
     * Get all active MB products (non-deleted and approved only)
     * Without pagination - returns all records
     *
     * @return List of all active MB products
     */
    List<MbPResponseDto> getAllActiveProducts();

}
package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.MbPRequestDto;
import com.gn.pharmacy.dto.response.MbPResponseDto;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.MbPEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MbPService {
    MbPResponseDto createMbProduct(MbPRequestDto dto);
    MbPResponseDto updateMbProduct(Long id, MbPRequestDto dto);
    MbPResponseDto patchMbProduct(Long id, MbPRequestDto dto);
    MbPResponseDto getMbProductById(Long id);
    MbPResponseDto getMbProductBySku(String sku);
    List<MbPResponseDto> getAllMbProduct();
    List<MbPResponseDto> getMbProductsByCategory(String category);
    List<MbPResponseDto> getMbProductsBySubCategory(String subCategory);
    List<MbPResponseDto> searchMbProducts(String keyword);
    boolean existsBySku(String sku);
    void deleteMbProduct(Long id);
    MbPResponseDto toDto(MbPEntity entity);


    //============= NEW METHOD ADDED=========//
    public Page<MbPResponseDto> getAllProducts(Pageable pageable);
    public List<MbPResponseDto> getAllProducts();

}
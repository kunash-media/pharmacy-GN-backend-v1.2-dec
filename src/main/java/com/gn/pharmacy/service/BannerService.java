package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.BannerRequestDto;
import com.gn.pharmacy.dto.response.BannerResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BannerService {

    BannerResponseDto createBanner(BannerRequestDto dto, List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo, MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception;

    BannerResponseDto getBannerById(Long id);

    List<BannerResponseDto> getAllBanners();

    BannerResponseDto updateBanner(Long id, BannerRequestDto dto, List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo, MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception;

    void deleteBanner(Long id);

    byte[] getBannerSlideImage(Long id, int index);

    byte[] getBannerFileTwoImage(Long id);

    byte[] getBannerFileThreeImage(Long id);

    byte[] getBannerFileFourImage(Long id);

    BannerResponseDto getBannerByPageName(String pageName);
}
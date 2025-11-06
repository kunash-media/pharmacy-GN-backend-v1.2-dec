package com.gn.pharmacy.service.serviceImpl;


import com.gn.pharmacy.dto.request.BannerRequestDto;
import com.gn.pharmacy.dto.response.BannerResponseDto;
import com.gn.pharmacy.entity.BannerEntity;
import com.gn.pharmacy.repository.BannerRepository;
import com.gn.pharmacy.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {

    private static final Logger logger = LoggerFactory.getLogger(BannerServiceImpl.class);

    @Autowired
    private BannerRepository bannerRepository;

    @Override
    public BannerResponseDto createBanner(BannerRequestDto dto, List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo, MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception {
        logger.info("Creating banner for page: {}", dto.getPageName());

        BannerEntity entity = new BannerEntity();
        entity.setPageName(dto.getPageName());

        List<byte[]> slidesBytes = new ArrayList<>();
        if (bannerFileSlides != null && !bannerFileSlides.isEmpty()) {
            for (MultipartFile file : bannerFileSlides) {
                if (!file.isEmpty()) {
                    slidesBytes.add(file.getBytes());
                }
            }
        }
        entity.setBannerFileSlides(slidesBytes);

        if (bannerFileTwo != null && !bannerFileTwo.isEmpty()) {
            entity.setBannerFileTwo(bannerFileTwo.getBytes());
        }

        if (bannerFileThree != null && !bannerFileThree.isEmpty()) {
            entity.setBannerFileThree(bannerFileThree.getBytes());
        }

        if (bannerFileFour != null && !bannerFileFour.isEmpty()) {
            entity.setBannerFileFour(bannerFileFour.getBytes());
        }

        BannerEntity savedEntity = bannerRepository.save(entity);
        logger.info("Banner created with ID: {}", savedEntity.getBannerId());

        return convertToResponseDto(savedEntity);
    }


    @Override
    public BannerResponseDto getBannerByPageName(String pageName) {
        logger.info("Fetching banner for page name: {}", pageName);
        return bannerRepository.findByPageName(pageName)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> {
                    logger.warn("Banner not found for page name: {}", pageName);
                    return new RuntimeException("Banner not found for page name: " + pageName);
                });
    }




    @Override
    public BannerResponseDto getBannerById(Long id) {
        logger.info("Fetching banner with ID: {}", id);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            return convertToResponseDto(optionalEntity.get());
        } else {
            logger.warn("Banner not found with ID: {}", id);
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public List<BannerResponseDto> getAllBanners() {
        logger.info("Fetching all banners");
        List<BannerEntity> entities = bannerRepository.findAll();
        List<BannerResponseDto> dtos = new ArrayList<>();
        for (BannerEntity entity : entities) {
            dtos.add(convertToResponseDto(entity));
        }
        return dtos;
    }

    @Override
    public BannerResponseDto updateBanner(Long id, BannerRequestDto dto, List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo, MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception {
        logger.info("Updating banner with ID: {}", id);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            BannerEntity entity = optionalEntity.get();

            if (dto.getPageName() != null) {
                entity.setPageName(dto.getPageName());
            }

            if (bannerFileSlides != null && !bannerFileSlides.isEmpty()) {
                List<byte[]> slidesBytes = new ArrayList<>();
                for (MultipartFile file : bannerFileSlides) {
                    if (!file.isEmpty()) {
                        slidesBytes.add(file.getBytes());
                    }
                }
                entity.setBannerFileSlides(slidesBytes);
            }

            if (bannerFileTwo != null && !bannerFileTwo.isEmpty()) {
                entity.setBannerFileTwo(bannerFileTwo.getBytes());
            }

            if (bannerFileThree != null && !bannerFileThree.isEmpty()) {
                entity.setBannerFileThree(bannerFileThree.getBytes());
            }

            if (bannerFileFour != null && !bannerFileFour.isEmpty()) {
                entity.setBannerFileFour(bannerFileFour.getBytes());
            }

            BannerEntity updatedEntity = bannerRepository.save(entity);
            logger.info("Banner updated with ID: {}", updatedEntity.getBannerId());
            return convertToResponseDto(updatedEntity);
        } else {
            logger.warn("Banner not found for update with ID: {}", id);
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public void deleteBanner(Long id) {
        logger.info("Deleting banner with ID: {}", id);
        bannerRepository.deleteById(id);
    }

    @Override
    public byte[] getBannerSlideImage(Long id, int index) {
        logger.info("Fetching slide image for banner ID: {} at index: {}", id, index);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            List<byte[]> slides = optionalEntity.get().getBannerFileSlides();
            if (index >= 0 && index < slides.size()) {
                return slides.get(index);
            } else {
                throw new RuntimeException("Invalid slide index");
            }
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public byte[] getBannerFileTwoImage(Long id) {
        logger.info("Fetching file two image for banner ID: {}", id);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get().getBannerFileTwo();
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public byte[] getBannerFileThreeImage(Long id) {
        logger.info("Fetching file three image for banner ID: {}", id);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get().getBannerFileThree();
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public byte[] getBannerFileFourImage(Long id) {
        logger.info("Fetching file four image for banner ID: {}", id);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get().getBannerFileFour();
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    private BannerResponseDto convertToResponseDto(BannerEntity entity) {
        BannerResponseDto dto = new BannerResponseDto();
        dto.setId(entity.getBannerId());
        dto.setPageName(entity.getPageName());

        List<String> slideLinks = new ArrayList<>();
        for (int i = 0; i < entity.getBannerFileSlides().size(); i++) {
            slideLinks.add("/api/banners/" + entity.getBannerId() + "/slides/" + i);
        }
        dto.setBannerFileSlides(slideLinks);

        if (entity.getBannerFileTwo() != null) {
            dto.setBannerFileTwo("/api/banners/" + entity.getBannerId() + "/filetwo");
        }

        if (entity.getBannerFileThree() != null) {
            dto.setBannerFileThree("/api/banners/" + entity.getBannerId() + "/filethree");
        }

        if (entity.getBannerFileFour() != null) {
            dto.setBannerFileFour("/api/banners/" + entity.getBannerId() + "/filefour");
        }

        return dto;
    }
}


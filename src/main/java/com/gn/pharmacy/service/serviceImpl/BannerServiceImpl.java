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
import java.util.List;
import java.util.Optional;

@Service
public class BannerServiceImpl implements BannerService {

    private static final Logger logger = LoggerFactory.getLogger(BannerServiceImpl.class);

    @Autowired
    private BannerRepository bannerRepository;

    @Override
    public BannerResponseDto createBanner(
            BannerRequestDto dto,
            List<MultipartFile> bannerFileSlides,
            MultipartFile bannerFileTwo,
            MultipartFile bannerFileThree,
            MultipartFile bannerFileFour) throws Exception {

        logger.info("Creating banner for page: {}", dto.getPageName());

        String pageName = dto.getPageName();

        // Check if a banner with this pageName already exists
        if (bannerRepository.existsByPageName(pageName)) {
            throw new RuntimeException("page name existing already! delete existing page");
        }

        BannerEntity entity = new BannerEntity();
        entity.setPageName(pageName);

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
    public BannerResponseDto getBannerById(Long bannerId) {
        logger.info("Fetching banner with ID: {}", bannerId);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
        if (optionalEntity.isPresent()) {
            return convertToResponseDto(optionalEntity.get());
        } else {
            logger.warn("Banner not found with ID: {}", bannerId);
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

//    @Override
//    public BannerResponseDto updateBanner(Long bannerId, BannerRequestDto dto, List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo, MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception {
//        logger.info("Updating banner with ID: {}", bannerId);
//        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
//        if (optionalEntity.isPresent()) {
//            BannerEntity entity = optionalEntity.get();
//
//            if (dto.getPageName() != null) {
//                entity.setPageName(dto.getPageName());
//            }
//
//            if (bannerFileSlides != null && !bannerFileSlides.isEmpty()) {
//                List<byte[]> slidesBytes = new ArrayList<>();
//                for (MultipartFile file : bannerFileSlides) {
//                    if (!file.isEmpty()) {
//                        slidesBytes.add(file.getBytes());
//                    }
//                }
//                entity.setBannerFileSlides(slidesBytes);
//            }
//
//            if (bannerFileTwo != null && !bannerFileTwo.isEmpty()) {
//                entity.setBannerFileTwo(bannerFileTwo.getBytes());
//            }
//
//            if (bannerFileThree != null && !bannerFileThree.isEmpty()) {
//                entity.setBannerFileThree(bannerFileThree.getBytes());
//            }
//
//            if (bannerFileFour != null && !bannerFileFour.isEmpty()) {
//                entity.setBannerFileFour(bannerFileFour.getBytes());
//            }
//
//            BannerEntity updatedEntity = bannerRepository.save(entity);
//            logger.info("Banner updated with ID: {}", updatedEntity.getBannerId());
//            return convertToResponseDto(updatedEntity);
//        } else {
//            logger.warn("Banner not found for update with ID: {}", bannerId);
//            throw new RuntimeException("Banner not found");
//        }
//    }


    @Override
    public BannerResponseDto updateBanner(Long bannerId, BannerRequestDto dto,
                                          List<MultipartFile> bannerFileSlides, MultipartFile bannerFileTwo,
                                          MultipartFile bannerFileThree, MultipartFile bannerFileFour) throws Exception {

        logger.info("=== BANNER SERVICE UPDATE START ===");
        logger.info("Updating banner with ID: {}", bannerId);
        logger.info("PageName from DTO: {}", dto.getPageName());

        // Log all incoming files in detail
        logger.info("bannerFileSlides: {}",
                bannerFileSlides != null ? bannerFileSlides.size() + " file(s)" : "null");

        if (bannerFileSlides != null && !bannerFileSlides.isEmpty()) {
            for (int i = 0; i < bannerFileSlides.size(); i++) {
                MultipartFile file = bannerFileSlides.get(i);
                logger.info("  Slide {}: {} ({} bytes, Content-Type: {})",
                        i + 1,
                        file.getOriginalFilename(),
                        file.getSize(),
                        file.getContentType());
            }
        }

        logger.info("bannerFileTwo: {}",
                bannerFileTwo != null ?
                        String.format("%s (%d bytes, Content-Type: %s)",
                                bannerFileTwo.getOriginalFilename(),
                                bannerFileTwo.getSize(),
                                bannerFileTwo.getContentType()) : "null");

        logger.info("bannerFileThree: {}",
                bannerFileThree != null ?
                        String.format("%s (%d bytes, Content-Type: %s)",
                                bannerFileThree.getOriginalFilename(),
                                bannerFileThree.getSize(),
                                bannerFileThree.getContentType()) : "null");

        logger.info("bannerFileFour: {}",
                bannerFileFour != null ?
                        String.format("%s (%d bytes, Content-Type: %s)",
                                bannerFileFour.getOriginalFilename(),
                                bannerFileFour.getSize(),
                                bannerFileFour.getContentType()) : "null");

        // Find the existing banner
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
        if (!optionalEntity.isPresent()) {
            logger.error("Banner not found with ID: {}", bannerId);
            throw new RuntimeException("Banner not found with ID: " + bannerId);
        }

        BannerEntity entity = optionalEntity.get();
        logger.info("Found existing banner: ID={}, PageName={}",
                entity.getBannerId(), entity.getPageName());

        // Update page name if provided
        if (dto.getPageName() != null) {
            logger.info("Updating pageName from '{}' to '{}'",
                    entity.getPageName(), dto.getPageName());
            entity.setPageName(dto.getPageName());
        }

        // Update banner slides if provided
        if (bannerFileSlides != null && !bannerFileSlides.isEmpty()) {
            logger.info("Processing {} slide file(s)", bannerFileSlides.size());
            List<byte[]> slidesBytes = new ArrayList<>();
            for (MultipartFile file : bannerFileSlides) {
                if (!file.isEmpty()) {
                    byte[] fileBytes = file.getBytes();
                    slidesBytes.add(fileBytes);
                    logger.info("Added slide: {} ({} bytes)",
                            file.getOriginalFilename(), fileBytes.length);
                }
            }
            entity.setBannerFileSlides(slidesBytes);
            logger.info("Total slides stored: {}", slidesBytes.size());
        }

        // Update bannerFileTwo if provided
        if (bannerFileTwo != null && !bannerFileTwo.isEmpty()) {
            byte[] bytes = bannerFileTwo.getBytes();
            entity.setBannerFileTwo(bytes);
            logger.info("Set bannerFileTwo: {} ({} bytes)",
                    bannerFileTwo.getOriginalFilename(), bytes.length);
        } else {
            logger.info("bannerFileTwo not updated (null or empty)");
        }

        // Update bannerFileThree if provided
        if (bannerFileThree != null && !bannerFileThree.isEmpty()) {
            byte[] bytes = bannerFileThree.getBytes();
            entity.setBannerFileThree(bytes);
            logger.info("Set bannerFileThree: {} ({} bytes)",
                    bannerFileThree.getOriginalFilename(), bytes.length);
        } else {
            logger.info("bannerFileThree not updated (null or empty)");
        }

        // Update bannerFileFour if provided
        if (bannerFileFour != null && !bannerFileFour.isEmpty()) {
            byte[] bytes = bannerFileFour.getBytes();
            entity.setBannerFileFour(bytes);
            logger.info("Set bannerFileFour: {} ({} bytes)",
                    bannerFileFour.getOriginalFilename(), bytes.length);
        } else {
            logger.warn("bannerFileFour is null or empty! Not updating.");
        }

        // Save the entity
        logger.info("Saving banner entity...");
        try {
            BannerEntity updatedEntity = bannerRepository.save(entity);
            logger.info("Banner saved successfully with ID: {}", updatedEntity.getBannerId());

            // Verify the saved data
            logger.info("Verifying saved data:");
            logger.info("  - bannerFileTwo present: {}", updatedEntity.getBannerFileTwo() != null);
            logger.info("  - bannerFileThree present: {}", updatedEntity.getBannerFileThree() != null);
            logger.info("  - bannerFileFour present: {}", updatedEntity.getBannerFileFour() != null);
            logger.info("  - bannerFileSlides count: {}",
                    updatedEntity.getBannerFileSlides() != null ?
                            updatedEntity.getBannerFileSlides().size() : 0);

            // If bannerFileFour should be present but isn't, log warning
            if (bannerFileFour != null && !bannerFileFour.isEmpty() &&
                    updatedEntity.getBannerFileFour() == null) {
                logger.error("CRITICAL: bannerFileFour was provided but not saved to database!");
            }

            logger.info("=== BANNER SERVICE UPDATE END ===");
            return convertToResponseDto(updatedEntity);

        } catch (Exception e) {
            logger.error("Error saving banner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save banner: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBanner(Long bannerId) {
        logger.info("Deleting banner with ID: {}", bannerId);
        bannerRepository.deleteById(bannerId);
    }

    @Override
    public byte[] getBannerSlideImage(Long bannerId, int index) {
        logger.info("Fetching slide image for banner ID: {} at index: {}", bannerId, index);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
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
    public byte[] getBannerFileTwoImage(Long bannerId) {
        logger.info("Fetching file two image for banner ID: {}", bannerId);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get().getBannerFileTwo();
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public byte[] getBannerFileThreeImage(Long bannerId) {
        logger.info("Fetching file three image for banner ID: {}", bannerId);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get().getBannerFileThree();
        } else {
            throw new RuntimeException("Banner not found");
        }
    }

    @Override
    public byte[] getBannerFileFourImage(Long bannerId) {
        logger.info("Fetching file four image for banner ID: {}", bannerId);
        Optional<BannerEntity> optionalEntity = bannerRepository.findById(bannerId);
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


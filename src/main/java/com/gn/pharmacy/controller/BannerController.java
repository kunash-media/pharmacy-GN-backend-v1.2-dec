package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.BannerRequestDto;
import com.gn.pharmacy.dto.response.BannerResponseDto;
import com.gn.pharmacy.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    @Autowired
    private BannerService bannerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerResponseDto> createBanner(
            @RequestPart("pageName") String pageName,
            @RequestPart(value = "bannerFileSlides", required = false) List<MultipartFile> bannerFileSlides,
            @RequestPart(value = "bannerFileTwo", required = false) MultipartFile bannerFileTwo,
            @RequestPart(value = "bannerFileThree", required = false) MultipartFile bannerFileThree,
            @RequestPart(value = "bannerFileFour", required = false) MultipartFile bannerFileFour) throws Exception {
        logger.info("Received create banner request for page: {}", pageName);
        BannerRequestDto dto = new BannerRequestDto();
        dto.setPageName(pageName);
        BannerResponseDto responseDto = bannerService.createBanner(dto, bannerFileSlides, bannerFileTwo, bannerFileThree, bannerFileFour);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/get-Banner-By-Id/{bannerId}")
    public ResponseEntity<BannerResponseDto> getBannerById(@PathVariable Long bannerId) {
        logger.info("Received get banner request for ID: {}", bannerId);
        BannerResponseDto dto = bannerService.getBannerById(bannerId);
        return ResponseEntity.ok(dto);
    }
    // =============== NEW API ADDED =================//

    @GetMapping("/get-by-page-name/{pageName}")
    public ResponseEntity<BannerResponseDto> getBannerByPageName(@PathVariable String pageName) {
        logger.info("Received request to get banner by page name: {}", pageName);
        BannerResponseDto dto = bannerService.getBannerByPageName(pageName);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-All-Banners")
    public ResponseEntity<List<BannerResponseDto>> getAllBanners() {
        logger.info("Received get all banners request");
        List<BannerResponseDto> dtos = bannerService.getAllBanners();
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping(value = "/update-Banner/{bannerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerResponseDto> updateBanner(
            @PathVariable Long bannerId,
            @RequestPart(value = "pageName", required = false) String pageName,
            @RequestPart(value = "bannerFileSlides", required = false) List<MultipartFile> bannerFileSlides,
            @RequestPart(value = "bannerFileTwo", required = false) MultipartFile bannerFileTwo,
            @RequestPart(value = "bannerFileThree", required = false) MultipartFile bannerFileThree,
            @RequestPart(value = "bannerFileFour", required = false) MultipartFile bannerFileFour) throws Exception {

        logger.info("=== UPDATE BANNER REQUEST START ===");
        logger.info("Received update banner request for ID: {}", bannerId);
        logger.info("pageName: {}", pageName);
        logger.info("bannerFileSlides: {}", bannerFileSlides != null ? bannerFileSlides.size() + " file(s)" : "null");
        logger.info("bannerFileTwo: {}", bannerFileTwo != null ?
                "Present - " + bannerFileTwo.getOriginalFilename() + " (" + bannerFileTwo.getSize() + " bytes)" : "null");
        logger.info("bannerFileThree: {}", bannerFileThree != null ?
                "Present - " + bannerFileThree.getOriginalFilename() + " (" + bannerFileThree.getSize() + " bytes)" : "null");
        logger.info("bannerFileFour: {}", bannerFileFour != null ?
                "Present - " + bannerFileFour.getOriginalFilename() + " (" + bannerFileFour.getSize() + " bytes)" : "null");

        BannerRequestDto dto = new BannerRequestDto();
        if (pageName != null) {
            dto.setPageName(pageName);
        }

        try {
            BannerResponseDto responseDto = bannerService.updateBanner(bannerId, dto, bannerFileSlides, bannerFileTwo, bannerFileThree, bannerFileFour);
            logger.info("=== UPDATE BANNER REQUEST END - SUCCESS ===");
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            logger.error("=== UPDATE BANNER REQUEST END - ERROR ===");
            logger.error("Error updating banner: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/delete-Banner/{bannerId}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long bannerId) {
        logger.info("Received delete banner request for ID: {}", bannerId);
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping(value = "/get-banner-slide-image/{bannerId}/slides/{index}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerSlideImage(@PathVariable Long bannerId, @PathVariable int index) {
        logger.info("Received get slide image request for banner ID: {} index: {}", bannerId, index);
        byte[] image = bannerService.getBannerSlideImage(bannerId, index);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Two-Image/{bannerId}/filetwo", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileTwoImage(@PathVariable Long bannerId) {
        logger.info("Received get file two image request for banner ID: {}", bannerId);
        byte[] image = bannerService.getBannerFileTwoImage(bannerId);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Three-Image/{bannerId}/filethree", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileThreeImage(@PathVariable Long bannerId) {
        logger.info("Received get file three image request for banner ID: {}", bannerId);
        byte[] image = bannerService.getBannerFileThreeImage(bannerId);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Four-Image/{bannerId}/filefour", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileFourImage(@PathVariable Long bannerId) {
        logger.info("Received get file four image request for banner ID: {}", bannerId);
        byte[] image = bannerService.getBannerFileFourImage(bannerId);
        return ResponseEntity.ok(image);
    }


}

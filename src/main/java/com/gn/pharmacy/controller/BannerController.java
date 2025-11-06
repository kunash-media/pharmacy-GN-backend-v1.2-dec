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

    @GetMapping("/get-Banner-By-Id/{id}")
    public ResponseEntity<BannerResponseDto> getBannerById(@PathVariable Long id) {
        logger.info("Received get banner request for ID: {}", id);
        BannerResponseDto dto = bannerService.getBannerById(id);
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

    @PatchMapping(value = "/update-Banner/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerResponseDto> updateBanner(
            @PathVariable Long id,
            @RequestPart(value = "pageName", required = false) String pageName,
            @RequestPart(value = "bannerFileSlides", required = false) List<MultipartFile> bannerFileSlides,
            @RequestPart(value = "bannerFileTwo", required = false) MultipartFile bannerFileTwo,
            @RequestPart(value = "bannerFileThree", required = false) MultipartFile bannerFileThree,
            @RequestPart(value = "bannerFileFour", required = false) MultipartFile bannerFileFour) throws Exception {
        logger.info("Received update banner request for ID: {}", id);
        BannerRequestDto dto = new BannerRequestDto();
        if (pageName != null) {
            dto.setPageName(pageName);
        }
        BannerResponseDto responseDto = bannerService.updateBanner(id, dto, bannerFileSlides, bannerFileTwo, bannerFileThree, bannerFileFour);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete-Banner/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        logger.info("Received delete banner request for ID: {}", id);
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping(value = "/get-banner-slide-image/{id}/slides/{index}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerSlideImage(@PathVariable Long id, @PathVariable int index) {
        logger.info("Received get slide image request for banner ID: {} index: {}", id, index);
        byte[] image = bannerService.getBannerSlideImage(id, index);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Two-Image/{id}/filetwo", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileTwoImage(@PathVariable Long id) {
        logger.info("Received get file two image request for banner ID: {}", id);
        byte[] image = bannerService.getBannerFileTwoImage(id);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Three-Image/{id}/filethree", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileThreeImage(@PathVariable Long id) {
        logger.info("Received get file three image request for banner ID: {}", id);
        byte[] image = bannerService.getBannerFileThreeImage(id);
        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/get-Banner-File-Four-Image/{id}/filefour", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBannerFileFourImage(@PathVariable Long id) {
        logger.info("Received get file four image request for banner ID: {}", id);
        byte[] image = bannerService.getBannerFileFourImage(id);
        return ResponseEntity.ok(image);
    }


}

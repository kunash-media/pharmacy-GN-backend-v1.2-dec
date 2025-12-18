package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.PrescriptionRequestDTO;
import com.gn.pharmacy.dto.response.PrescriptionResponseDTO;
import com.gn.pharmacy.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);
    private static final String LOG_PREFIX = "[PrescriptionController]";

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
        logger.info("{} Controller initialized", LOG_PREFIX);
    }

    @PostMapping(value = "/create-order", consumes = {"multipart/form-data"})
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(
            @RequestPart("orderData") PrescriptionRequestDTO requestDTO,
            @RequestPart(value = "prescriptionImg", required = false) MultipartFile prescriptionImg) {

        logger.info("{} Creating prescription - UserId: {}, Email: {}, Doctor: {}",
                LOG_PREFIX, requestDTO.getUserId(), requestDTO.getEmail(), requestDTO.getDoctorName());

        PrescriptionResponseDTO response = prescriptionService.createPrescription(requestDTO, prescriptionImg);

        logger.info("{} Prescription created successfully - PrescriptionId: {}, Status: {}",
                LOG_PREFIX, response.getPrescriptionId(), response.getOrderStatus());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-by-prescriptionId/{prescriptionId}")
    public ResponseEntity<PrescriptionResponseDTO> getPrescriptionById(
            @PathVariable String prescriptionId) {

        logger.info("{} Fetching prescription by ID: {}", LOG_PREFIX, prescriptionId);
        PrescriptionResponseDTO response = prescriptionService.getPrescriptionById(prescriptionId);

        logger.debug("{} Retrieved prescription - ID: {}, Status: {}, Doctor: {}",
                LOG_PREFIX, prescriptionId, response.getOrderStatus(), response.getDoctorName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-orders-by-userId/{userId}")
    public ResponseEntity<Page<PrescriptionResponseDTO>> getAllPrescriptions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("{} Fetching prescriptions for UserId: {} - Page: {}, Size: {}, Sort: {}",
                LOG_PREFIX, userId, page, size, sortBy);

        Page<PrescriptionResponseDTO> responses = prescriptionService.getAllPrescriptions(userId, page, size, sortBy, sortDirection);

        logger.debug("{} Retrieved {} prescriptions for UserId: {}",
                LOG_PREFIX, responses.getTotalElements(), userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/get-all-orders")
    public ResponseEntity<Page<PrescriptionResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("{} Fetching all orders - Page: {}, Size: {}, Sort: {}",
                LOG_PREFIX, page, size, sortBy);

        Page<PrescriptionResponseDTO> responses = prescriptionService.getAllOrders(page, size, sortBy, sortDirection);

        logger.debug("{} Retrieved {} total orders", LOG_PREFIX, responses.getTotalElements());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/get-by-status")
    public ResponseEntity<Page<PrescriptionResponseDTO>> getPrescriptionsByStatus(
            @RequestParam("status") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("{} Fetching prescriptions by Status: {} - Page: {}, Size: {}",
                LOG_PREFIX, status, page, size);

        Page<PrescriptionResponseDTO> responses = prescriptionService.getPrescriptionsByStatus(status, page, size, sortBy, sortDirection);

        logger.debug("{} Retrieved {} prescriptions with Status: {}",
                LOG_PREFIX, responses.getTotalElements(), status);

        return ResponseEntity.ok(responses);
    }

    @PutMapping(value = "/update-by-prescriptionId/{prescriptionId}", consumes = {"multipart/form-data"})
    public ResponseEntity<PrescriptionResponseDTO> updatePrescription(
            @PathVariable String prescriptionId,
            @RequestPart("orderData") PrescriptionRequestDTO requestDTO,
            @RequestPart(value = "prescriptionImg", required = false) MultipartFile prescriptionImg,
            @RequestParam Long userId) {

        logger.info("{} Updating prescription - ID: {}, UserId: {}, Doctor: {}",
                LOG_PREFIX, prescriptionId, userId, requestDTO.getDoctorName());

        PrescriptionResponseDTO response = prescriptionService.updatePrescription(prescriptionId, requestDTO, prescriptionImg, userId);

        logger.info("{} Prescription updated successfully - ID: {}, Status: {}",
                LOG_PREFIX, prescriptionId, response.getOrderStatus());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{prescriptionId}/approve")
    public ResponseEntity<PrescriptionResponseDTO> approvePrescription(
            @PathVariable String prescriptionId,
            @RequestParam Boolean isApproved) {

        logger.info("{} Updating approval status - PrescriptionId: {}, IsApproved: {}",
                LOG_PREFIX, prescriptionId, isApproved);

        PrescriptionResponseDTO response = prescriptionService.updateApprovalStatus(prescriptionId, isApproved);

        logger.info("{} Approval status updated - PrescriptionId: {}, NewStatus: {}",
                LOG_PREFIX, prescriptionId, response.isApproved());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/patch-status/{prescriptionId}")
    public ResponseEntity<PrescriptionResponseDTO> updateOrderStatus(
            @PathVariable String prescriptionId,
            @RequestParam("status") String status) {

        logger.info("{} Updating order status - PrescriptionId: {}, NewStatus: {}",
                LOG_PREFIX, prescriptionId, status);

        PrescriptionResponseDTO response = prescriptionService.updateOrderStatus(prescriptionId, status);

        logger.info("{} Order status updated - PrescriptionId: {}, OldStatus: {}, NewStatus: {}",
                LOG_PREFIX, prescriptionId, response.getOrderStatus(), status);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reject-order-by-status/{prescriptionId}/reject-order")
    public ResponseEntity<PrescriptionResponseDTO> rejectOrder(@PathVariable String prescriptionId) {

        logger.info("{} Rejecting order - PrescriptionId: {}", LOG_PREFIX, prescriptionId);

        PrescriptionResponseDTO response = prescriptionService.rejectOrder(prescriptionId);

        logger.info("{} Order rejected - PrescriptionId: {}, Status: {}",
                LOG_PREFIX, prescriptionId, response.getOrderStatus());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{prescriptionId}/image")
    public ResponseEntity<byte[]> getPrescriptionImage(
            @PathVariable String prescriptionId,
            @RequestParam Long userId) {

        logger.info("{} Fetching prescription image - PrescriptionId: {}, UserId: {}",
                LOG_PREFIX, prescriptionId, userId);

        byte[] image = prescriptionService.getPrescriptionImage(prescriptionId, userId);

        if (image == null) {
            logger.warn("{} Image not found - PrescriptionId: {}, UserId: {}",
                    LOG_PREFIX, prescriptionId, userId);
            return ResponseEntity.notFound().build();
        }

        logger.debug("{} Image retrieved successfully - PrescriptionId: {}, Size: {} bytes",
                LOG_PREFIX, prescriptionId, image.length);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @DeleteMapping("/delete-order-by-prescriptionId/{prescriptionId}")
    public ResponseEntity<String> deletePrescription(@PathVariable String prescriptionId) {

        logger.warn("{} Deleting prescription - PrescriptionId: {}", LOG_PREFIX, prescriptionId);

        prescriptionService.deletePrescription(prescriptionId);

        logger.info("{} Prescription deleted successfully - PrescriptionId: {}",
                LOG_PREFIX, prescriptionId);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Order Deleted Successfully!! with id : " + prescriptionId);
    }
}
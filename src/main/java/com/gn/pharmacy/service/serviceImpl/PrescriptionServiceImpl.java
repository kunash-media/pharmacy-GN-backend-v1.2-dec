package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.PrescriptionRequestDTO;
import com.gn.pharmacy.dto.response.PrescriptionResponseDTO;
import com.gn.pharmacy.entity.PrescriptionEntity;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.repository.PrescriptionRepository;
import com.gn.pharmacy.repository.UserRepository;
import com.gn.pharmacy.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionServiceImpl.class);
    private static final String LOG_PREFIX = "[PrescriptionService]";

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                   UserRepository userRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        logger.info("{} Service initialized", LOG_PREFIX);
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO, MultipartFile imageFile) {
        logger.info("{} Creating new prescription - UserId: {}, Doctor: {}",
                LOG_PREFIX, requestDTO.getUserId(), requestDTO.getDoctorName());

        if (imageFile != null && !imageFile.isEmpty()) {
            logger.debug("{} Image file received - Name: {}, Size: {} bytes, ContentType: {}",
                    LOG_PREFIX, imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());
        } else {
            logger.warn("{} No image file received for prescription creation", LOG_PREFIX);
        }

        try {
            UserEntity user = userRepository.findById(requestDTO.getUserId())
                    .orElseThrow(() -> {
                        logger.error("{} User not found - UserId: {}", LOG_PREFIX, requestDTO.getUserId());
                        return new RuntimeException("User not found with id: " + requestDTO.getUserId());
                    });

            PrescriptionEntity entity = new PrescriptionEntity();
            mapToEntity(requestDTO, entity);
            entity.setUser(user);

            String generatedId = generateUniquePrescriptionId();
            entity.setPrescriptionId(generatedId);
            logger.info("{} Generated unique prescriptionId: {}", LOG_PREFIX, generatedId);

            byte[] imageBytes = convertToByteArray(imageFile);
            if (imageBytes != null) {
                logger.debug("{} Image converted to byte array - Size: {} bytes", LOG_PREFIX, imageBytes.length);
            }
            entity.setPrescriptionImg(imageBytes);

            PrescriptionEntity saved = prescriptionRepository.save(entity);
            logger.info("{} Prescription created successfully - PrescriptionId: {}, Status: {}",
                    LOG_PREFIX, saved.getPrescriptionId(), saved.getOrderStatus());

            return mapToResponse(saved);
        } catch (IOException e) {
            logger.error("{} Error creating prescription - Failed to process image: {}",
                    LOG_PREFIX, e.getMessage(), e);
            throw new RuntimeException("Failed to process image file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("{} Unexpected error creating prescription: {}", LOG_PREFIX, e.getMessage(), e);
            throw new RuntimeException("Failed to create prescription: " + e.getMessage());
        }
    }

    @Override
    public PrescriptionResponseDTO getPrescriptionById(String prescriptionId) {
        logger.info("{} Fetching prescription by ID: {}", LOG_PREFIX, prescriptionId);

        PrescriptionEntity entity = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found - PrescriptionId: {}", LOG_PREFIX, prescriptionId);
                    return new RuntimeException("Prescription not found for ID: " + prescriptionId);
                });

        logger.debug("{} Prescription retrieved - ID: {}, Status: {}, Doctor: {}",
                LOG_PREFIX, prescriptionId, entity.getOrderStatus(), entity.getDoctorName());

        return mapToResponse(entity);
    }

    @Override
    public Page<PrescriptionResponseDTO> getAllPrescriptions(Long userId, int page, int size, String sortBy, String sortDirection) {
        logger.info("{} Fetching prescriptions for UserId: {} - Page: {}, Size: {}, Sort: {}",
                LOG_PREFIX, userId, page, size, sortBy);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PrescriptionEntity> entities = prescriptionRepository.findByUserUserId(userId, pageable);

        logger.debug("{} Retrieved {} prescriptions for UserId: {}",
                LOG_PREFIX, entities.getTotalElements(), userId);

        return entities.map(this::mapToResponse);
    }

    @Override
    public Page<PrescriptionResponseDTO> getAllOrders(int page, int size, String sortBy, String sortDirection) {
        logger.info("{} Fetching all orders - Page: {}, Size: {}, Sort: {}",
                LOG_PREFIX, page, size, sortBy);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PrescriptionEntity> entities = prescriptionRepository.findAll(pageable);

        logger.debug("{} Retrieved {} total orders", LOG_PREFIX, entities.getTotalElements());
        return entities.map(this::mapToResponse);
    }

    @Override
    public Page<PrescriptionResponseDTO> getPrescriptionsByStatus(String status, int page, int size, String sortBy, String sortDirection) {
        logger.info("{} Fetching prescriptions by Status: {} - Page: {}, Size: {}",
                LOG_PREFIX, status, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PrescriptionEntity> entities = prescriptionRepository.findByOrderStatus(status, pageable);

        logger.debug("{} Retrieved {} prescriptions with Status: {}",
                LOG_PREFIX, entities.getTotalElements(), status);

        return entities.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO updatePrescription(String prescriptionId, PrescriptionRequestDTO requestDTO,
                                                      MultipartFile imageFile, Long userId) {
        logger.info("{} Updating prescription - PrescriptionId: {}, UserId: {}, Doctor: {}",
                LOG_PREFIX, prescriptionId, userId, requestDTO.getDoctorName());

        PrescriptionEntity entity = prescriptionRepository.findByUserUserIdAndPrescriptionId(userId, prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for update - PrescriptionId: {}, UserId: {}",
                            LOG_PREFIX, prescriptionId, userId);
                    return new RuntimeException("Prescription not found for ID: " + prescriptionId + " and userId: " + userId);
                });

        try {
            mapToEntity(requestDTO, entity);

            if (imageFile != null && !imageFile.isEmpty()) {
                entity.setPrescriptionImg(convertToByteArray(imageFile));
                logger.debug("{} Prescription image updated - PrescriptionId: {}", LOG_PREFIX, prescriptionId);
            }

            PrescriptionEntity updated = prescriptionRepository.save(entity);
            logger.info("{} Prescription updated successfully - PrescriptionId: {}, Status: {}",
                    LOG_PREFIX, prescriptionId, updated.getOrderStatus());

            return mapToResponse(updated);
        } catch (IOException e) {
            logger.error("{} Error updating prescription image - PrescriptionId: {}: {}",
                    LOG_PREFIX, prescriptionId, e.getMessage(), e);
            throw new RuntimeException("Failed to process image file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("{} Unexpected error updating prescription - PrescriptionId: {}: {}",
                    LOG_PREFIX, prescriptionId, e.getMessage(), e);
            throw new RuntimeException("Failed to update prescription: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO updateApprovalStatus(String prescriptionId, Boolean isApproved) {
        logger.info("{} Updating approval status - PrescriptionId: {}, IsApproved: {}",
                LOG_PREFIX, prescriptionId, isApproved);

        PrescriptionEntity entity = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for approval update - PrescriptionId: {}",
                            LOG_PREFIX, prescriptionId);
                    return new RuntimeException("Prescription not found with ID: " + prescriptionId);
                });

        boolean oldStatus = entity.isApproved();
        entity.setApproved(isApproved);
        PrescriptionEntity updated = prescriptionRepository.save(entity);

        logger.info("{} Approval status updated - PrescriptionId: {}, Old: {}, New: {}",
                LOG_PREFIX, prescriptionId, oldStatus, isApproved);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO updateOrderStatus(String prescriptionId, String status) {
        logger.info("{} Updating order status - PrescriptionId: {}, NewStatus: {}",
                LOG_PREFIX, prescriptionId, status);

        PrescriptionEntity entity = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for status update - PrescriptionId: {}",
                            LOG_PREFIX, prescriptionId);
                    return new RuntimeException("Prescription not found with ID: " + prescriptionId);
                });

        String oldStatus = entity.getOrderStatus();
        entity.setOrderStatus(status);
        PrescriptionEntity updated = prescriptionRepository.save(entity);

        logger.info("{} Order status updated - PrescriptionId: {}, OldStatus: {}, NewStatus: {}",
                LOG_PREFIX, prescriptionId, oldStatus, status);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO rejectOrder(String prescriptionId) {
        logger.info("{} Rejecting order - PrescriptionId: {}", LOG_PREFIX, prescriptionId);

        PrescriptionEntity entity = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for rejection - PrescriptionId: {}",
                            LOG_PREFIX, prescriptionId);
                    return new RuntimeException("Prescription not found with ID: " + prescriptionId);
                });

        String oldStatus = entity.getOrderStatus();
        entity.setOrderStatus("REJECTED");
        entity.setApproved(false);
        PrescriptionEntity updated = prescriptionRepository.save(entity);

        logger.info("{} Order rejected - PrescriptionId: {}, OldStatus: {}, NewStatus: REJECTED",
                LOG_PREFIX, prescriptionId, oldStatus);

        return mapToResponse(updated);
    }

    @Override
    public byte[] getPrescriptionImage(String prescriptionId, Long userId) {
        logger.info("{} Fetching prescription image - PrescriptionId: {}, UserId: {}",
                LOG_PREFIX, prescriptionId, userId);

        PrescriptionEntity entity = prescriptionRepository.findByUserUserIdAndPrescriptionId(userId, prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for image retrieval - PrescriptionId: {}, UserId: {}",
                            LOG_PREFIX, prescriptionId, userId);
                    return new RuntimeException("Prescription not found for ID: " + prescriptionId + " and userId: " + userId);
                });

        byte[] image = entity.getPrescriptionImg();
        if (image == null) {
            logger.warn("{} No image found for prescription - PrescriptionId: {}", LOG_PREFIX, prescriptionId);
        } else {
            logger.debug("{} Image retrieved - PrescriptionId: {}, Size: {} bytes",
                    LOG_PREFIX, prescriptionId, image.length);
        }

        return image;
    }

    @Override
    @Transactional
    public void deletePrescription(String prescriptionId) {
        logger.warn("{} Deleting prescription - PrescriptionId: {}", LOG_PREFIX, prescriptionId);

        PrescriptionEntity entity = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> {
                    logger.error("{} Prescription not found for deletion - PrescriptionId: {}",
                            LOG_PREFIX, prescriptionId);
                    return new RuntimeException("Prescription not found for ID: " + prescriptionId);
                });

        prescriptionRepository.delete(entity);
        logger.info("{} Prescription deleted successfully - PrescriptionId: {}", LOG_PREFIX, prescriptionId);
    }

    private void mapToEntity(PrescriptionRequestDTO dto, PrescriptionEntity entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setMobileNumber(dto.getMobileNumber());
        entity.setEmail(dto.getEmail());
        entity.setOrderStatus(dto.getOrderStatus());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setDoctorName(dto.getDoctorName()); // Added doctor name mapping
        entity.setApproved(dto.isApproved());
    }

    private PrescriptionResponseDTO mapToResponse(PrescriptionEntity entity) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setPrescriptionId(entity.getPrescriptionId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setMobileNumber(entity.getMobileNumber());
        dto.setEmail(entity.getEmail());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setOrderStatus(entity.getOrderStatus());
        dto.setImageUrl("/api/prescriptions/" + entity.getPrescriptionId() + "/image");
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setApproved(entity.isApproved());
        dto.setDoctorName(entity.getDoctorName()); // Added doctor name mapping
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId());
        }
        return dto;
    }

    private byte[] convertToByteArray(MultipartFile file) throws IOException {
        return file != null && !file.isEmpty() ? file.getBytes() : null;
    }

    private String generateUniquePrescriptionId() {
        String id;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            int randomNum = ThreadLocalRandom.current().nextInt(100000);
            id = "RX" + String.format("%05d", randomNum);
            attempts++;

            if (attempts > maxAttempts) {
                logger.error("{} Failed to generate unique prescription ID after {} attempts",
                        LOG_PREFIX, maxAttempts);
                throw new RuntimeException("Unable to generate unique prescription ID");
            }

        } while (prescriptionRepository.findByPrescriptionId(id).isPresent());

        logger.debug("{} Generated unique ID after {} attempts: {}", LOG_PREFIX, attempts, id);
        return id;
    }
}
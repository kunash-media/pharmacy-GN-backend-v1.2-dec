package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.bcrypt.BcryptEncoderConfig;
import com.gn.pharmacy.dto.request.AdminRequestDto;
import com.gn.pharmacy.dto.response.AdminResponseDto;
import com.gn.pharmacy.entity.AdminEntity;
import com.gn.pharmacy.repository.AdminRepository;
import com.gn.pharmacy.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminRepository adminRepository;
    private final BcryptEncoderConfig bcryptEncoderConfig;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository, BcryptEncoderConfig bcryptEncoderConfig) {
        this.adminRepository = adminRepository;
        this.bcryptEncoderConfig = bcryptEncoderConfig;
    }

    @Override
    public AdminResponseDto createAdmin(AdminRequestDto adminRequestDto) {
        logger.debug("Creating new admin with email: {}", adminRequestDto.getEmail());
        AdminEntity adminEntity = convertToEntity(adminRequestDto);
        adminEntity.setRole("ROLE_ADMIN"); // Default role
        AdminEntity savedAdmin = adminRepository.save(adminEntity);
        logger.debug("Admin saved with ID: {} and role: ROLE_ADMIN", savedAdmin.getId());
        return convertToResponseDto(savedAdmin);
    }

    @Override
    public AdminResponseDto getAdminById(Long id) {
        logger.debug("Fetching admin by ID: {}", id);
        AdminEntity adminEntity = adminRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Admin not found with id: {}", id);
                    return new RuntimeException("Admin not found with id: " + id);
                });
        return convertToResponseDto(adminEntity);
    }

    @Override
    public AdminResponseDto getAdminByEmail(String email) {
        logger.debug("Fetching admin by email: {}", email);
        AdminEntity adminEntity = adminRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Admin not found with email: {}", email);
                    return new RuntimeException("Admin not found with email: " + email);
                });
        return convertToResponseDto(adminEntity);
    }

    @Override
    public AdminResponseDto getAdminByPhoneNumber(String phoneNumber) {
        logger.debug("Fetching admin by phone number: {}", phoneNumber);
        AdminEntity adminEntity = adminRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    logger.error("Admin not found with phone number: {}", phoneNumber);
                    return new RuntimeException("Admin not found with phone number: " + phoneNumber);
                });
        return convertToResponseDto(adminEntity);
    }

    @Override
    public List<AdminResponseDto> getAllAdmins() {
        logger.debug("Fetching all admins");
        List<AdminEntity> admins = adminRepository.findAll();
        logger.debug("Found {} admins", admins.size());
        return admins.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponseDto updateAdmin(Long id, AdminRequestDto adminRequestDto) {
        logger.debug("Updating admin with ID: {}", id);
        AdminEntity existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Admin not found with id: {}", id);
                    return new RuntimeException("Admin not found with id: " + id);
                });
        existingAdmin.setFirstName(adminRequestDto.getFirstName());
        existingAdmin.setLastName(adminRequestDto.getLastName());
        existingAdmin.setPhoneNumber(adminRequestDto.getPhoneNumber());
        existingAdmin.setEmail(adminRequestDto.getEmail());
        if (adminRequestDto.getPassword() != null && !adminRequestDto.getPassword().isEmpty()) {
            existingAdmin.setPassword(bcryptEncoderConfig.encode(adminRequestDto.getPassword()));
        }
        AdminEntity updatedAdmin = adminRepository.save(existingAdmin);
        logger.debug("Admin updated successfully with ID: {}", id);
        return convertToResponseDto(updatedAdmin);
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        logger.debug("Changing password for admin ID: {}", id);
        AdminEntity adminEntity = adminRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Admin not found with ID: {}", id);
                    return new RuntimeException("Admin not found with ID: " + id);
                });
        if (!bcryptEncoderConfig.matches(oldPassword, adminEntity.getPassword())) {
            logger.debug("Old password verification failed for admin ID: {}", id);
            return false;
        }
        adminEntity.setPassword(bcryptEncoderConfig.encode(newPassword));
        adminRepository.save(adminEntity);
        logger.debug("Password updated successfully for admin ID: {}", id);
        return true;
    }

    @Override
    public AdminResponseDto updateAdminPartial(Long id, AdminRequestDto adminRequestDto) {
        logger.debug("Patching admin with ID: {}", id);
        AdminEntity existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Admin not found with id: {}", id);
                    return new RuntimeException("Admin not found with id: " + id);
                });
        if (adminRequestDto.getFirstName() != null) existingAdmin.setFirstName(adminRequestDto.getFirstName());
        if (adminRequestDto.getLastName() != null) existingAdmin.setLastName(adminRequestDto.getLastName());
        if (adminRequestDto.getPhoneNumber() != null) existingAdmin.setPhoneNumber(adminRequestDto.getPhoneNumber());
        if (adminRequestDto.getEmail() != null) existingAdmin.setEmail(adminRequestDto.getEmail());
        if (adminRequestDto.getPassword() != null && !adminRequestDto.getPassword().isEmpty()) {
            existingAdmin.setPassword(bcryptEncoderConfig.encode(adminRequestDto.getPassword()));
        }
        AdminEntity updatedAdmin = adminRepository.save(existingAdmin);
        logger.debug("Admin patched successfully with ID: {}", id);
        return convertToResponseDto(updatedAdmin);
    }

    @Override
    public void deleteAdmin(Long id) {
        logger.debug("Deleting admin with ID: {}", id);
        if (!adminRepository.existsById(id)) {
            logger.error("Admin not found with id: {}", id);
            throw new RuntimeException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
        logger.debug("Admin deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists: {}", email);
        return adminRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        logger.debug("Checking if phone number exists: {}", phoneNumber);
        return adminRepository.existsByPhoneNumber(phoneNumber);
    }

    private AdminEntity convertToEntity(AdminRequestDto requestDto) {
        AdminEntity entity = new AdminEntity();
        entity.setFirstName(requestDto.getFirstName());
        entity.setLastName(requestDto.getLastName());
        entity.setPhoneNumber(requestDto.getPhoneNumber());
        entity.setEmail(requestDto.getEmail());
        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            entity.setPassword(bcryptEncoderConfig.encode(requestDto.getPassword()));
        }
        return entity;
    }

    private AdminResponseDto convertToResponseDto(AdminEntity entity) {
        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setId(entity.getId());
        responseDto.setFirstName(entity.getFirstName());
        responseDto.setLastName(entity.getLastName());
        responseDto.setPhoneNumber(entity.getPhoneNumber());
        responseDto.setEmail(entity.getEmail());
//        responseDto.setPassword(entity.getPassword());
        responseDto.setRole(entity.getRole());
        return responseDto;
    }
}
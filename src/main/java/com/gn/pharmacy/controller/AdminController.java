package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.AdminRequestDto;
import com.gn.pharmacy.dto.response.AdminResponseDto;
import com.gn.pharmacy.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-admin")
    public ResponseEntity<AdminResponseDto> createAdmin(@RequestBody AdminRequestDto adminRequestDto) {
        logger.info("Request received to create admin with email: {}", adminRequestDto.getEmail());
        AdminResponseDto responseDto = adminService.createAdmin(adminRequestDto);
        logger.info("Admin created successfully with ID: {} and role: ROLE_ADMIN", responseDto.getId());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/get-admin-by-id/{id}")
    public ResponseEntity<AdminResponseDto> getAdminById(@PathVariable Long id) {
        logger.info("Request received to get admin by ID: {}", id);
        AdminResponseDto responseDto = adminService.getAdminById(id);
        logger.info("Admin retrieved successfully with ID: {}", id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-by-email/{email}")
    public ResponseEntity<AdminResponseDto> getAdminByEmail(@PathVariable String email) {
        logger.info("Request received to get admin by email: {}", email);
        AdminResponseDto responseDto = adminService.getAdminByEmail(email);
        logger.info("Admin retrieved successfully with email: {}", email);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-by-phone/{phoneNumber}")
    public ResponseEntity<AdminResponseDto> getAdminByPhoneNumber(@PathVariable String phoneNumber) {
        logger.info("Request received to get admin by phone number: {}", phoneNumber);
        AdminResponseDto responseDto = adminService.getAdminByPhoneNumber(phoneNumber);
        logger.info("Admin retrieved successfully with phone number: {}", phoneNumber);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-all-admins")
    public ResponseEntity<List<AdminResponseDto>> getAllAdmins() {
        logger.info("Request received to get all admins");
        List<AdminResponseDto> admins = adminService.getAllAdmins();
        logger.info("Retrieved {} admins successfully", admins.size());
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/put-admin-by-id/{id}")
    public ResponseEntity<AdminResponseDto> updateAdmin(@PathVariable Long id, @RequestBody AdminRequestDto adminRequestDto) {
        logger.info("Request received to update admin with ID: {}", id);
        AdminResponseDto responseDto = adminService.updateAdmin(id, adminRequestDto);
        logger.info("Admin updated successfully with ID: {}", id);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/patch-admin-by-id/{id}")
    public ResponseEntity<AdminResponseDto> updateAdminPartial(@PathVariable Long id, @RequestBody AdminRequestDto adminRequestDto) {
        logger.info("Request received to patch admin with ID: {}", id);
        AdminResponseDto responseDto = adminService.updateAdminPartial(id, adminRequestDto);
        logger.info("Admin patched successfully with ID: {}", id);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete-admin-by-id/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        logger.info("Request received to delete admin with ID: {}", id);
        adminService.deleteAdmin(id);
        logger.info("Admin deleted successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body("Admin Deleted!! with ID: " + id);
    }
}
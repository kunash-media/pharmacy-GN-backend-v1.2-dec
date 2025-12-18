package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.UserDTO;
import com.gn.pharmacy.dto.request.UserRequestDto;
import com.gn.pharmacy.dto.response.UserResponseDto;
import com.gn.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDto userRequestDto) {
        logger.info("Request received to create user with email: {}", userRequestDto.getEmail());

        // Check if email already exists
        if (userService.isEmailExists(userRequestDto.getEmail())) {
            logger.warn("Email already exists: {}", userRequestDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already exists: " + userRequestDto.getEmail());
        }

        UserResponseDto response = userService.createUser(userRequestDto);
        logger.info("User created successfully with ID: {}", response.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("mobile") String mobile,
                                   @RequestParam("password") String password) {
        try {
            UserDTO user = userService.authenticateUser(mobile, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Login failed for mobile: {}", mobile, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/get-by-user-id/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        logger.info("Request received to get user by ID: {}", userId);
        UserResponseDto response = userService.getUserById(userId);
        logger.info("User retrieved successfully with ID: {}", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        logger.info("Request received to get all users");
        List<UserResponseDto> response = userService.getAllUsers();
        logger.info("Retrieved {} users successfully", response.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-user-by-id/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId,
                                                      @RequestBody UserRequestDto userRequestDto) {
        logger.info("Request received to update user with ID: {}", userId);
        UserResponseDto response = userService.updateUser(userId, userRequestDto);
        logger.info("User updated successfully with ID: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/patch-user-by-id/{userId}")
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable Long userId,
                                                     @RequestBody UserRequestDto userRequestDto) {
        logger.info("Request received to patch user with ID: {}", userId);
        UserResponseDto response = userService.patchUser(userId, userRequestDto);
        logger.info("User patched successfully with ID: {}", userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-by-user-id/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        logger.info("Request received to delete user with ID: {}", userId);
        userService.deleteUser(userId);
        logger.info("User deleted successfully with ID: {}", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body("User deleted!! with user id: " + userId);
    }
}
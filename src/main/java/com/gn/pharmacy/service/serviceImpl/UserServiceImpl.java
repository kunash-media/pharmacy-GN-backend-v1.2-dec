package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.bcrypt.BcryptEncoderConfig;
import com.gn.pharmacy.dto.request.UserDTO;
import com.gn.pharmacy.dto.request.UserRequestDto;
import com.gn.pharmacy.dto.response.UserResponseDto;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.repository.UserRepository;
import com.gn.pharmacy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BcryptEncoderConfig bcryptEncoderConfig;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BcryptEncoderConfig bcryptEncoderConfig) {
        this.userRepository = userRepository;
        this.bcryptEncoderConfig = bcryptEncoderConfig;
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.debug("Creating new user with email: {}", userRequestDto.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            log.error("Email already exists: {}", userRequestDto.getEmail());
            throw new RuntimeException("Email already exists: " + userRequestDto.getEmail());
        }
        UserEntity userEntity = new UserEntity();
        mapRequestToEntity(userRequestDto, userEntity);
        UserEntity savedEntity = userRepository.save(userEntity);
        log.debug("User saved with ID: {}", savedEntity.getUserId());
        return mapEntityToResponse(savedEntity);
    }

    @Override
    public UserDTO authenticateUser(String mobile, String password) {
        // Find user by mobile/phone
        UserEntity user = userRepository.findByPhone(mobile)
                .orElseThrow(() -> new RuntimeException("User not found with mobile: " + mobile));

        // Check if password matches using BCrypt
        if (!bcryptEncoderConfig.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Convert to DTO to e  xclude password
        return new UserDTO(user);
    }


    @Override
    public UserResponseDto getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });
        return mapEntityToResponse(userEntity);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponseDto> users = userRepository.findAll().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} users", users.size());
        return users;
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto userRequestDto) {
        log.debug("Updating user with ID: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found with ID: " + userId);
                });
        mapRequestToEntity(userRequestDto, userEntity);
        UserEntity updatedEntity = userRepository.save(userEntity);
        log.debug("User updated successfully with ID: {}", userId);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public UserResponseDto patchUser(Long userId, UserRequestDto userRequestDto) {

        log.debug("Patching user with ID: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new RuntimeException("User not found with ID: " + userId);
            });

        // Only update fields that are not null in the request
        if (userRequestDto.getFirstName() != null) {
            userEntity.setFirstName(userRequestDto.getFirstName());
        }
        if (userRequestDto.getLastName() != null) {
            userEntity.setLastName(userRequestDto.getLastName());
        }
        if (userRequestDto.getEmail() != null) {
            userEntity.setEmail(userRequestDto.getEmail());
        }
        if (userRequestDto.getPhone() != null) {
            userEntity.setPhone(userRequestDto.getPhone());
        }
        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            userEntity.setPassword(bcryptEncoderConfig.encode(userRequestDto.getPassword()));
        }
        if (userRequestDto.getAddressLandmark() != null) {
            userEntity.setAddressLandmark(userRequestDto.getAddressLandmark());
        }
        if (userRequestDto.getAddressArea() != null) {
            userEntity.setAddressArea(userRequestDto.getAddressArea());
        }
        if (userRequestDto.getAddressCity() != null) {
            userEntity.setAddressCity(userRequestDto.getAddressCity());
        }
        if (userRequestDto.getAddressPincode() != null) {
            userEntity.setAddressPincode(userRequestDto.getAddressPincode());
        }
        if (userRequestDto.getAddressState() != null) {
            userEntity.setAddressState(userRequestDto.getAddressState());
        }
        if (userRequestDto.getAddressCountry() != null) {
            userEntity.setAddressCountry(userRequestDto.getAddressCountry());
        }
        if (userRequestDto.getAddressType() != null) {
            userEntity.setAddressType(userRequestDto.getAddressType());
        }

        UserEntity patchedEntity = userRepository.save(userEntity);
        log.debug("User patched successfully with ID: {}", userId);
        return mapEntityToResponse(patchedEntity);
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
        log.debug("User deleted successfully with ID: {}", userId);
    }

    private void mapRequestToEntity(UserRequestDto requestDto, UserEntity entity) {
        // Map firstName and lastName directly
        entity.setFirstName(requestDto.getFirstName());
        entity.setLastName(requestDto.getLastName());
        entity.setEmail(requestDto.getEmail());
        entity.setPhone(requestDto.getPhone());

        // Encrypt the password only if provided
        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            entity.setPassword(bcryptEncoderConfig.encode(requestDto.getPassword()));
        }

        // Map address fields
        entity.setAddressLandmark(requestDto.getAddressLandmark());
        entity.setAddressArea(requestDto.getAddressArea());
        entity.setAddressCity(requestDto.getAddressCity());
        entity.setAddressPincode(requestDto.getAddressPincode());
        entity.setAddressState(requestDto.getAddressState());
        entity.setAddressCountry(requestDto.getAddressCountry());
        entity.setAddressType(requestDto.getAddressType());
    }

    private UserResponseDto mapEntityToResponse(UserEntity entity) {
        UserResponseDto responseDto = new UserResponseDto();

        responseDto.setUserId(entity.getUserId());
        responseDto.setFirstName(entity.getFirstName());
        responseDto.setLastName(entity.getLastName());
        responseDto.setPhone(entity.getPhone());
        responseDto.setEmail(entity.getEmail());
        responseDto.setAddressLandmark(entity.getAddressLandmark());
        responseDto.setAddressArea(entity.getAddressArea());
        responseDto.setAddressCity(entity.getAddressCity());
        responseDto.setAddressPincode(entity.getAddressPincode());
        responseDto.setAddressState(entity.getAddressState());
        responseDto.setAddressCountry(entity.getAddressCountry());
        responseDto.setAddressType(entity.getAddressType());
        return responseDto;
    }
}